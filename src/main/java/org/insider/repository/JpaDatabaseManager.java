package org.insider.repository;

import jakarta.persistence.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.insider.model.Transaction;
import org.insider.repository.entities.SymbolsEntity;
import org.insider.repository.entities.TransactionsEntity;

import java.sql.Date;
import java.time.LocalDate;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;

public class JpaDatabaseManager implements DatabaseManager {
    private static final Logger logger = LogManager.getLogger(JpaDatabaseManager.class);
    private final EntityManagerFactory entityManagerFactory;

    public JpaDatabaseManager(EntityManagerFactory entityManagerFactory) {
        this.entityManagerFactory = entityManagerFactory;
    }

    @Override
    public List<Transaction> getTransactionsByRange(String symbol, String region, String startDate, String endDate) {
        List<Transaction> transactions = Collections.emptyList();

        try (EntityManager entityManager = entityManagerFactory.createEntityManager()) {
            TypedQuery<TransactionsEntity> query = entityManager.createQuery(
                    "SELECT t " +
                    "FROM TransactionsEntity t " +
                    "WHERE t.symbol =: symbol " +
                    "AND t.region =: region " +
                    "AND t.startDate >=: startDate " +
                    "AND t.startDate <=: endDate",
                    TransactionsEntity.class);
            query.setParameter("symbol", symbol);
            query.setParameter("region", region);
            query.setParameter("startDate", Date.valueOf(startDate));
            query.setParameter("endDate", Date.valueOf(endDate));

            transactions = convertTransactionsEntityToTransaction(query.getResultList());

        } catch (IllegalStateException e) {
            logger.error(e.getMessage());
        }

        return transactions;
    }

    @Override
    public void saveTransactions(List<Transaction> transactions, String symbol, String region) {
        performSaveOperation(entityManager -> {
            int batchSize = 500;

            // TODO: This is currently based on the last updated date, all transactions that are below
            // this date will be filtered out, so not added to the database. But this can still cause
            // issues, in case new transactions with same date are included into the source after we
            // called it. Need to check the database for the very last record instead, and with this
            // information, filter out all transactions before this one.
            SymbolsEntity symbolRecord = null;

            try {
                symbolRecord = getSymbolRecord(symbol, region);
            } catch (NoResultException ignored) {
            }

            List<Transaction> transactionsToSave;

            if (symbolRecord != null) {
                LocalDate lastUpdated = symbolRecord.getUpdated();
                transactionsToSave = transactions.stream()
                        .filter(t -> !t.startDateAsLocalDate().isBefore(lastUpdated)).toList();
            } else {
                transactionsToSave = transactions;
            }

            updateSymbolRecord(symbol, region);

            for (int i = 0; i < transactionsToSave.size(); i++) {
                Transaction t = transactionsToSave.get(i);
                TransactionsEntity tradeRecord = new TransactionsEntity(
                        symbol,
                        region,
                        t.getFilerName(),
                        t.getTransactionText(),
                        t.getMoneyText(),
                        t.getOwnership(),
                        Date.valueOf(t.getStartDate()),
                        Double.valueOf(t.getValue()),
                        t.getFilerRelation(),
                        Integer.valueOf(t.getShares()),
                        t.getFilerUrl(),
                        Math.toIntExact(t.getMaxAge()),
                        t.getSide(),
                        t.getPrice()
                );
                entityManager.persist(tradeRecord);
                logger.info("Persisted transaction entity: " + tradeRecord);

                if (i % batchSize == 0) {
                    entityManager.flush();
                    entityManager.clear();
                }
            }
        }, "Committed transaction entity list to the database");
    }

    @Override
    public void closeEntityManagerFactory() {
        entityManagerFactory.close();
    }

    private void updateSymbolRecord(String symbol, String region) {
        performSaveOperation(entityManager -> {
            SymbolsEntity symbolRecord = null;

            try {
                symbolRecord = getSymbolRecord(symbol, region);
            } catch (NoResultException ignored) {
            }

            if (symbolRecord == null) {
                entityManager.persist(
                        new SymbolsEntity(symbol, region, LocalDate.now())
                );
            } else {
                symbolRecord.setUpdated(LocalDate.now());
                entityManager.merge(symbolRecord);
            }
        }, "Committed symbol updated date to database");
    }

    @Override
    public SymbolsEntity getSymbolRecord(String symbol, String region) throws NoResultException {
        return performQuery(entityManager -> {
            TypedQuery<SymbolsEntity> query = entityManager.createQuery(
                    "SELECT s " +
                        "FROM SymbolsEntity s " +
                        "WHERE s.symbol =: symbol " +
                        "AND s.region =: region",
                SymbolsEntity.class);
            query.setParameter("symbol", symbol);
            query.setParameter("region", region);

            return query.getSingleResult();
        });
    }

    private void performSaveOperation(Consumer<EntityManager> action, String successLog) {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        EntityTransaction entityTransaction = entityManager.getTransaction();

        try (entityManager) {
            entityTransaction.begin();
            action.accept(entityManager);
            entityTransaction.commit();

            logger.info(successLog);
        } catch (RuntimeException e) {
            if (entityTransaction.isActive()) {
                entityTransaction.rollback();
            }

            logger.error(e.getMessage());
            throw e;
        }
    }

    private <T> T performQuery(Function<EntityManager, T> action) throws NoResultException {
        EntityManager entityManager = entityManagerFactory.createEntityManager();

        try (entityManager) {
            return action.apply(entityManager);
        } catch (NoResultException e) {
            logger.warn(e.getMessage());
            throw e;
        }
    }

    private List<Transaction> convertTransactionsEntityToTransaction(List<TransactionsEntity> transactionEntities) {
        List<Transaction> transactions = new ArrayList<>();

        for (var entity : transactionEntities) {
            transactions.add(new Transaction(
                    entity.getFilerName(),
                    entity.getTransactionText(),
                    entity.getMoneyText(),
                    entity.getOwnership(),
                    new Transaction.LocalDateWrapper(entity.getStartDate().toLocalDate()),
                    String.valueOf(entity.getValue()),
                    entity.getFilerRelation(),
                    String.valueOf(entity.getShares()),
                    entity.getFilerUrl(),
                    entity.getMaxAge(),
                    entity.getSide(),
                    entity.getPrice()
            ));
        }

        return transactions;
    }
}
