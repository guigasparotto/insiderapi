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
import java.util.stream.Collectors;

public class DatabaseManager {
    private static final Logger logger = LogManager.getLogger(DatabaseManager.class);
    private final EntityManagerFactory entityManagerFactory;

    public DatabaseManager() {
        // Creates the session factory
        entityManagerFactory = Persistence.createEntityManagerFactory("default",
            new HashMap<String, String>() {
                {
                    put("hibernate.connection.username", ConfigReader.getProperty("database.username"));
                    put("hibernate.connection.password", ConfigReader.getProperty("database.password"));
                }
        });
    }

    public List<Transaction> getByDateRange(String symbol, String region, String startDate, String endDate) {

        try (EntityManager entityManager = entityManagerFactory.createEntityManager()) {
            TypedQuery<TransactionsEntity> jpqlQuery = entityManager.createQuery(
                    "SELECT t " +
                    "FROM TransactionsEntity t " +
                    "WHERE t.symbol =: symbol " +
                    "AND t.region =: region " +
                    "AND t.startDate >=: startDate " +
                    "AND t.startDate <=: endDate",
                    TransactionsEntity.class);
            jpqlQuery.setParameter("symbol", symbol);
            jpqlQuery.setParameter("region", region);
            jpqlQuery.setParameter("startDate", Date.valueOf(startDate));
            jpqlQuery.setParameter("endDate", Date.valueOf(endDate));

            return transactionsEntityToTransaction(jpqlQuery.getResultList());

        } catch (IllegalStateException e) {
            logger.error(e.getMessage());
        }

        return null;
    }

    public void saveToDatabase(List<Transaction> transactions, String symbol, String region) {
        // Creates the session
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        EntityTransaction entityTransaction = entityManager.getTransaction();
        int batchSize = 500;

        try (entityManager) {
            entityTransaction.begin();

            for (int i = 0; i < transactions.size(); i++) {
                Transaction t = transactions.get(i);
                TransactionsEntity entity = new TransactionsEntity();
                entity.setSymbol(symbol);
                entity.setRegion(region);
                entity.setFilerName(t.getFilerName());
                entity.setFilerRelation(t.getFilerRelation());
                entity.setFilerUrl(t.getFilerUrl());
                entity.setOwnership(t.getOwnership());
                entity.setTransactionText(t.getTransactionText());
                entity.setStartDate(Date.valueOf(t.getStartDate()));
                entity.setShares(Integer.valueOf(t.getShares()));
                entity.setValue(Double.valueOf(t.getValue()));
                entity.setMoneyText(t.getMoneyText());
                entity.setMaxAge(Math.toIntExact(t.getMaxAge()));
                entityManager.persist(entity);
                logger.info("Persisted transaction entity: " + entity);

                if (i % batchSize == 0) {
                    entityManager.flush();
                    entityManager.clear();
                }
            }

            entityTransaction.commit();
            logger.info("Committed transaction entity list to the database");
        } catch (IllegalStateException e) {
            logger.error(e.getMessage());

            if (entityTransaction.isActive()) {
                entityTransaction.rollback();
            }
        }
    }

    public void closeEntityManagerFactory() {
        entityManagerFactory.close();
    }

    private List<Transaction> transactionsEntityToTransaction(List<TransactionsEntity> transactionEntities) {
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
