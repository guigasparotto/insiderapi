package org.insider.model;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class Transaction {
    private final String filerName;
    private final String transactionText;
    private final String moneyText;
    private final String ownership;
    private final LocalDateWrapper startDate;
    private final String value;
    private final String filerRelation;
    private final String shares;
    private final String filerUrl;
    private final Integer maxAge;

    public Transaction(String filerName,
                       String transactionText,
                       String moneyText,
                       String ownership,
                       LocalDateWrapper startDate,
                       String value,
                       String filerRelation,
                       String shares,
                       String filerUrl,
                       Integer maxAge)
    {
        this.filerName = filerName;
        this.transactionText = transactionText;
        this.moneyText = moneyText;
        this.ownership = ownership;
        this.startDate = startDate;
        this.value = value;
        this.filerRelation = filerRelation;
        this.shares = shares;
        this.filerUrl = filerUrl;
        this.maxAge = maxAge;
    }

    public String getFilerName() { return filerName; }

    public String getTransactionText() { return transactionText; }

    public String getMoneyText() { return moneyText; }

    public String getOwnership() { return ownership; }

    public String getStartDate() { return startDate.getLocalDate(); }

    public String getValue() { return value; }

    public String getFilerRelation() { return filerRelation; }

    public String getShares() { return shares; }

    public String getFilerUrl() { return filerUrl; }

    public Integer getMaxAge() { return maxAge; }

    @Override
    public String toString() {
        return "Transaction{" +
                "filerName='" + filerName + '\'' +
                ", transactionText='" + transactionText + '\'' +
                ", moneyText='" + moneyText + '\'' +
                ", ownership='" + ownership + '\'' +
                ", startDate=" + startDate +
                ", value='" + value + '\'' +
                ", filerRelation='" + filerRelation + '\'' +
                ", shares='" + shares + '\'' +
                ", filerUrl='" + filerUrl + '\'' +
                ", maxAge=" + maxAge +
                '}';
    }

    public static class LocalDateWrapper {
        private final LocalDate localDate;
        private final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        public LocalDateWrapper(LocalDate localDate) {
            this.localDate = localDate;
        }

        public String getLocalDate() {
            return localDate.format(dateTimeFormatter);
        }

        public LocalDate asLocalDate() {
            return this.localDate;
        }

        @Override
        public String toString() {
            return localDate.format(dateTimeFormatter);
        }
    }
}


