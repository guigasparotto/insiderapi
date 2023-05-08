package org.insider.repository;

import jakarta.persistence.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.insider.model.Transaction;
import org.insider.util.ConfigReader;

import java.sql.Date;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class DatabaseManager {
    private static final Logger logger = LogManager.getLogger(DatabaseManager.class);
    private final EntityManagerFactory entityManagerFactory;

    public DatabaseManager() {
        // Creates the session factory
        entityManagerFactory = Persistence.createEntityManagerFactory("default",
            new HashMap<String, String>() {
                {
                    put("hibernate.connection.url", ConfigReader.getProperty("database.url"));
                    put("hibernate.connection.username", ConfigReader.getProperty("database.username"));
                    put("hibernate.connection.password", ConfigReader.getProperty("database.password"));
                }
        });
    }

    public List<Transaction> getTransactionsByRange(String symbol, String region, String startDate, String endDate) {

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

            return convertTransactionsEntityToTransaction(query.getResultList());

        } catch (IllegalStateException e) {
            logger.error(e.getMessage());
        }

        return null;
    }

    public boolean saveToDatabase(List<Transaction> transactions, String symbol, String region) {
        // Creates the session
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        EntityTransaction entityTransaction = entityManager.getTransaction();
        int batchSize = 500;

        try (entityManager) {
            entityTransaction.begin();

            updateSymbolRecord(symbol, region);

            for (int i = 0; i < transactions.size(); i++) {
                Transaction t = transactions.get(i);
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
                        Math.toIntExact(t.getMaxAge())
                );
                entityManager.persist(tradeRecord);
                logger.info("Persisted transaction entity: " + tradeRecord);

                if (i % batchSize == 0) {
                    entityManager.flush();
                    entityManager.clear();
                }
            }

            entityTransaction.commit();
            logger.info("Committed transaction entity list to the database");

            return true;
        } catch (IllegalStateException e) {
            logger.error(e.getMessage());

            if (entityTransaction.isActive()) {
                entityTransaction.rollback();
            }

            return false;
        }
    }

    public void closeEntityManagerFactory() {
        entityManagerFactory.close();
    }

    private void updateSymbolRecord(String symbol, String region) {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        EntityTransaction entityTransaction = entityManager.getTransaction();

        try (entityManager) {
            entityTransaction.begin();

            SymbolsEntity symbolRecord = getSymbolRecord(symbol, region);

            if (symbolRecord == null) {
                symbolRecord = new SymbolsEntity(symbol, region, Date.valueOf(LocalDate.now()));
            } else {
                symbolRecord.setUpdated(Date.valueOf(LocalDate.now()));
            }

            entityManager.persist(symbolRecord);
            entityTransaction.commit();
            logger.info("Committed symbol updated date to database");
        } catch (IllegalStateException e) {
            logger.error(e.getMessage());

            if (entityTransaction.isActive()) {
                entityTransaction.rollback();
            }
        }
    }

    public SymbolsEntity getSymbolRecord(String symbol, String region) {
        SymbolsEntity symbolRecord = null;

        try (EntityManager entityManager = entityManagerFactory.createEntityManager()) {
            TypedQuery<SymbolsEntity> query = entityManager.createQuery(
                    "SELECT s " +
                    "FROM SymbolsEntity s " +
                    "WHERE s.symbol =: symbol " +
                    "AND s.region =: region",
                    SymbolsEntity.class);
            query.setParameter("symbol", symbol);
            query.setParameter("region", region);

            try {
                symbolRecord = query.getSingleResult();
            } catch (NoResultException e) {
                logger.warn(e.getMessage());
            } catch (IllegalStateException e) {
                logger.error(e.getMessage());
            }
        }

        return symbolRecord;
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
                    entity.getMaxAge()
            ));
        }

        return transactions;
    }
}
