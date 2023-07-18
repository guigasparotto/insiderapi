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
        return performQuery(entityManager -> {
            List<Transaction> transactions;
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

            return transactions;
        });
    }

    @Override
    public void saveTransactions(List<Transaction> transactions, String symbol, String region) {
        Objects.requireNonNull(transactions, "transactions cannot be null");
        Objects.requireNonNull(symbol, "symbol cannot be null");
        Objects.requireNonNull(region, "region cannot be null");

        // TODO: Need to improve these validations to cover other possible invalid cases
        if (transactions.isEmpty() || symbol.trim().isEmpty() || region.trim().isEmpty()) {
            throw new IllegalArgumentException("All fields must be non-empty");
        }

        performSaveOperation(entityManager -> {
            int batchSize = 500;

            // TODO: This is currently based on the last updated date, all transactions that are below
            // this date will be filtered out, so not added to the database. But this can still cause
            // issues, in case new transactions with same date are included into the source after we
            // called it. Need to check the database for the very last record instead, and with this
            // information, filter out all transactions before this one.
            Optional<SymbolsEntity> symbolRecord = getSymbolRecord(symbol, region);

            List<Transaction> transactionsToSave = transactions.stream()
                    .filter(t -> symbolRecord.map(
                            s -> !t.startDateAsLocalDate().isBefore(s.getUpdated())).orElse(true))
                    .toList();

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
        performSaveOperation(entityManager -> getSymbolRecord(symbol, region).ifPresentOrElse(
                symbolRecord -> {
                    symbolRecord.setUpdated(LocalDate.now());
                    entityManager.merge(symbolRecord);
                },
                () -> entityManager.persist(
                        new SymbolsEntity(symbol, region, LocalDate.now())
                )
        ), "Committed symbol updated date to database");
    }

    @Override
    public Optional<SymbolsEntity> getSymbolRecord(String symbol, String region) {
        return performQuery(entityManager -> {
            TypedQuery<SymbolsEntity> query = entityManager.createQuery(
                    "SELECT s " +
                            "FROM SymbolsEntity s " +
                            "WHERE s.symbol =: symbol " +
                            "AND s.region =: region",
                    SymbolsEntity.class);
            query.setParameter("symbol", symbol);
            query.setParameter("region", region);

            try {
                return Optional.ofNullable(query.getSingleResult());
            } catch (NoResultException e) {
                return Optional.empty();
            }
        });
    }

    private void performSaveOperation(Consumer<EntityManager> action, String successLog) {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        EntityTransaction entityTransaction = entityManager.getTransaction();

        try {
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
        } finally {
            if (entityManager.isOpen()) {
                entityManager.close();
            }
        }
    }

    private <T> T performQuery(Function<EntityManager, T> action) throws NoResultException {
        EntityManager entityManager = entityManagerFactory.createEntityManager();

        try {
            return action.apply(entityManager);
        } catch (NoResultException e) {
            logger.warn(e.getMessage());
            throw e;
        } finally {
            if (entityManager.isOpen()) {
                entityManager.close();
            }
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
