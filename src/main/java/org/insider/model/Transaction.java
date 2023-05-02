package org.insider.model;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class Transaction {
    private String filerName;
    private String transactionText;
    private String moneyText;
    private String ownership;
    private LocalDateWrapper startDate;
    private String value;
    private String filerRelation;
    private String shares;
    private String filerUrl;
    private Long maxAge;

    public Transaction(String filerName,
                       String transactionText,
                       String moneyText,
                       String ownership,
                       LocalDateWrapper startDate,
                       String value,
                       String filerRelation,
                       String shares,
                       String filerUrl,
                       Long maxAge)
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
    public void setFilerName(String value) { this.filerName = value; }

    public String getTransactionText() { return transactionText; }
    public void setTransactionText(String value) { this.transactionText = value; }

    public String getMoneyText() { return moneyText; }
    public void setMoneyText(String value) { this.moneyText = value; }

    public String getOwnership() { return ownership; }
    public void setOwnership(String value) { this.ownership = value; }

    public String getStartDate() { return startDate.getLocalDate(); }
    public void setStartDate(LocalDateWrapper value) { this.startDate = value; }

    public String getValue() { return value; }
    public void setValue(String value) { this.value = value; }

    public String getFilerRelation() { return filerRelation; }
    public void setFilerRelation(String value) { this.filerRelation = value; }

    public String getShares() { return shares; }
    public void setShares(String value) { this.shares = value; }

    public String getFilerUrl() { return filerUrl; }
    public void setFilerUrl(String value) { this.filerUrl = value; }

    public Long getMaxAge() { return maxAge; }
    public void setMaxAge(Long value) { this.maxAge = value; }

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
    }
}


