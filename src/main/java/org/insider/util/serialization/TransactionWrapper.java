package org.insider.util.serialization;

import org.insider.model.Transaction;

import java.util.List;

public class TransactionWrapper {
    private List<Transaction> transactions;

    public TransactionWrapper(List<Transaction> transactions) {
        this.transactions = transactions;
    }

    public List<Transaction> getTransactions() {
        return transactions;
    }
}
