package org.insider.repository;

import org.insider.model.Transaction;
import org.insider.repository.entities.SymbolsEntity;

import java.util.List;
import java.util.Optional;

public interface DatabaseManager {
    List<Transaction> getTransactionsByRange(String symbol, String region, String startDate, String endDate);

    void saveTransactions(List<Transaction> transactions, String symbol, String region);

    void closeEntityManagerFactory();

    Optional<SymbolsEntity> getSymbolRecord(String symbol, String region);
}
