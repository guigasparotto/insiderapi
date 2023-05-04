package org.insider.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.Persistence;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.insider.model.Transaction;
import org.insider.util.ConfigReader;

import java.sql.Date;
import java.util.HashMap;
import java.util.List;

public class DatabaseWrapper {
    private static final Logger logger = LogManager.getLogger(DatabaseWrapper.class);
    private final EntityManagerFactory entityManagerFactory;

    public DatabaseWrapper() {
        entityManagerFactory = Persistence.createEntityManagerFactory("default",
            new HashMap<String, String>() {
                {
                    put("hibernate.connection.username", ConfigReader.getProperty("database.username"));
                    put("hibernate.connection.password", ConfigReader.getProperty("database.password"));
                }
        });
    }

    public void saveToDatabase(List<Transaction> transactions) {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        EntityTransaction entityTransaction = entityManager.getTransaction();

        int batchSize = 500;

        try {
            entityTransaction.begin();

            for (int i = 0; i < transactions.size(); i++) {
                Transaction t = transactions.get(i);
                TransactionsEntity entity = new TransactionsEntity();
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
        } finally {
            entityManager.clear();
        }
    }

    public void closeEntityManagerFactory() {
        entityManagerFactory.close();
    }
}
