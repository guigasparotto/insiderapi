package org.insider.util;

import org.insider.model.Transaction;
import org.insider.repository.entities.TransactionsEntity;

import java.sql.Date;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class TestUtils {
    public static List<Transaction> createTransactions(int quantity, String date) {
        List<Transaction> transactions = new ArrayList<>();

        for (var i = 1; i <= quantity; i++) {
            transactions.add(new Transaction(
                    "Guilherme D",
                    "Bought at price " + (double) i + " per share.",
                    "Money text",
                    "D",
                    new Transaction.LocalDateWrapper(LocalDate.parse(date)),
                    String.valueOf(i * 1000),
                    "",
                    "1000",
                    "www.ggd.com",
                    1,
                    "buy",
                    (double) i)
            );
        }

        return transactions;
    }

    public static List<TransactionsEntity> createTransactionEntities(
            int quantity, String symbol, String region, Date date)
    {
        List<TransactionsEntity> transactions = new ArrayList<>();

        for (var i = 1; i <= quantity; i++) {
            transactions.add(new TransactionsEntity(
                    symbol, region,
                    "Guilherme D",
                    "Bought at price " + (double) i + " per share.",
                    "Money text", "D", date,
                    (double) (i * 1000), "", 1000,
                    "www.ggd.com", 1, "buy", (double) i
            ));
        }

        return transactions;
    }

    public static List<Transaction> convertTransactionEntitiesToTransactions(List<TransactionsEntity> transactionEntities) {
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
