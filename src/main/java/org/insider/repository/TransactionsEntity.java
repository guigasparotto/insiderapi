package org.insider.repository;

import jakarta.persistence.*;

import java.sql.Date;
import java.util.Objects;

@Entity
@Table(name = "transactions", schema = "public", catalog = "insider_trade")
public class TransactionsEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private String symbol;
    private String region;

    @Column(name = "filerName", nullable = false)
    private String filerName;

    private String transactionText;
    private String moneyText;
    private String ownership;

    @Column(nullable = false)
    private Date startDate;
    private Double value;
    private String filerRelation;
    private Integer shares;
    private String filerUrl;
    private Integer maxAge;

    // Hibernate requires a default constructor with at least protected visibility
    protected TransactionsEntity() {
    }

    public TransactionsEntity(
            String symbol,
            String region,
            String filerName,
            String transactionText,
            String moneyText,
            String ownership,
            Date startDate,
            Double value,
            String filerRelation,
            Integer shares,
            String filerUrl,
            Integer maxAge)
    {
        this.symbol = Objects.requireNonNull(symbol);
        this.region = Objects.requireNonNull(region);
        this.filerName = Objects.requireNonNull(filerName);
        this.transactionText = transactionText;
        this.moneyText = moneyText;
        this.ownership = ownership;
        this.startDate = Objects.requireNonNull(startDate);
        this.value = value;
        this.filerRelation = filerRelation;
        this.shares = Objects.requireNonNull(shares);
        this.filerUrl = filerUrl;
        this.maxAge = maxAge;
    }

    public int getId() {
        return id;
    }

    public String getSymbol() {
        return symbol;
    }

    public String getRegion() {
        return region;
    }

    public String getFilerName() {
        return filerName;
    }

    public String getTransactionText() {
        return transactionText;
    }

    public String getMoneyText() {
        return moneyText;
    }

    public String getOwnership() {
        return ownership;
    }

    public Date getStartDate() {
        return startDate;
    }

    public Double getValue() {
        return value;
    }

    public String getFilerRelation() {
        return filerRelation;
    }

    public Integer getShares() {
        return shares;
    }

    public String getFilerUrl() {
        return filerUrl;
    }

    public Integer getMaxAge() {
        return maxAge;
    }

    @Override
    public String toString() {
        return "TransactionsEntity{" +
                "id=" + id +
                ", filerName='" + filerName + '\'' +
                ", transactionText='" + transactionText + '\'' +
                ", moneyText='" + moneyText + '\'' +
                ", ownership='" + ownership + '\'' +
                ", startDate=" + startDate +
                ", value=" + value +
                ", filerRelation='" + filerRelation + '\'' +
                ", shares=" + shares +
                ", filerUrl='" + filerUrl + '\'' +
                ", maxAge=" + maxAge +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TransactionsEntity that = (TransactionsEntity) o;
        return id == that.id
               && Objects.equals(filerName, that.filerName)
               && Objects.equals(transactionText, that.transactionText)
               && Objects.equals(moneyText, that.moneyText)
               && Objects.equals(ownership, that.ownership)
               && Objects.equals(startDate, that.startDate)
               && Objects.equals(value, that.value)
               && Objects.equals(filerRelation, that.filerRelation)
               && Objects.equals(shares, that.shares)
               && Objects.equals(filerUrl, that.filerUrl)
               && Objects.equals(maxAge, that.maxAge);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, filerName, transactionText, moneyText, ownership,
                startDate, value, filerRelation, shares, filerUrl, maxAge);
    }
}
