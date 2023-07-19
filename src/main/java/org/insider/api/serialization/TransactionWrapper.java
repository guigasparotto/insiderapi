package org.insider.api.serialization;

import org.insider.model.Transaction;

import java.util.List;

public record TransactionWrapper(List<Transaction> transactions) {}
