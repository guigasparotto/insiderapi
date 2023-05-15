package org.insider.model;

import java.sql.Date;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.Objects;

public class Transaction implements Comparable<Transaction> {
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
    private final String side;
    private final Double price;

    public Transaction(String filerName,
                       String transactionText,
                       String moneyText,
                       String ownership,
                       LocalDateWrapper startDate,
                       String value,
                       String filerRelation,
                       String shares,
                       String filerUrl,
                       Integer maxAge,
                       String side,
                       Double price)
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
        this.side = side;
        this.price = price;
    }

    public String getFilerName() { return filerName; }

    public String getTransactionText() { return transactionText; }

    public String getMoneyText() { return moneyText; }

    public String getOwnership() { return ownership; }

    public String getStartDate() { return startDate.getLocalDate(); }

    public LocalDate getStartDateAsDate() { return LocalDate.parse(startDate.getLocalDate()); }

    public String getValue() { return value; }

    public String getFilerRelation() { return filerRelation; }

    public String getShares() { return shares; }

    public String getFilerUrl() { return filerUrl; }

    public Integer getMaxAge() { return maxAge; }

    public String getSide() { return side; }

    public Double getPrice() { return price; }

    @Override
    public String toString() {
        return "Transaction{" +
                "filerName='" + filerName + '\'' +
                ", transactionText='" + transactionText + '\'' +
                ", moneyText='" + moneyText + '\'' +
                ", ownership='" + ownership + '\'' +
                ", startDate=" + startDate + '\'' +
                ", side=" + side + '\'' +
                ", price=" + price + '\'' +
                ", value='" + value + '\'' +
                ", filerRelation='" + filerRelation + '\'' +
                ", shares='" + shares + '\'' +
                ", filerUrl='" + filerUrl + '\'' +
                ", maxAge=" + maxAge +
                '}';
    }

    @Override
    public int compareTo(Transaction other) {
        return Date.valueOf(this.getStartDate())
                .compareTo(Date.valueOf(other.getStartDate()));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Transaction that = (Transaction) o;
        return Objects.equals(filerName, that.filerName)
               && Objects.equals(transactionText, that.transactionText)
               && Objects.equals(moneyText, that.moneyText)
               && Objects.equals(ownership, that.ownership)
               && Objects.equals(startDate, that.startDate)
               && Objects.equals(value, that.value)
               && Objects.equals(side, that.side)
               && Objects.equals(price, that.price)
               && Objects.equals(filerRelation, that.filerRelation)
               && Objects.equals(shares, that.shares)
               && Objects.equals(filerUrl, that.filerUrl)
               && Objects.equals(maxAge, that.maxAge);
    }

    @Override
    public int hashCode() {
        return Objects.hash(filerName, transactionText, moneyText, ownership, startDate,
                value, side, price, filerRelation, shares, filerUrl, maxAge);
    }

    // TODO: Created to practice something, probably not needed
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
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            LocalDateWrapper that = (LocalDateWrapper) o;
            return Objects.equals(localDate, that.localDate);
        }

        @Override
        public int hashCode() {
            return Objects.hash(localDate, dateTimeFormatter);
        }

        @Override
        public String toString() {
            return localDate.format(dateTimeFormatter);
        }
    }
}


