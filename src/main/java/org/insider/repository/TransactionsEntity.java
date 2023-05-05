package org.insider.repository;

import jakarta.persistence.*;

import java.sql.Date;
import java.util.Objects;

@Entity
@Table(name = "transactions", schema = "public", catalog = "insider_trade")
public class TransactionsEntity {

    @Id
    @Column(name = "id")
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

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getRegion() {
        return region;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public String getFilerName() {
        return filerName;
    }

    public void setFilerName(String filerName) {
        this.filerName = filerName;
    }

    public String getTransactionText() {
        return transactionText;
    }

    public void setTransactionText(String transactionText) {
        this.transactionText = transactionText;
    }

    public String getMoneyText() {
        return moneyText;
    }

    public void setMoneyText(String moneyText) {
        this.moneyText = moneyText;
    }

    public String getOwnership() {
        return ownership;
    }

    public void setOwnership(String ownership) {
        this.ownership = ownership;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Double getValue() {
        return value;
    }

    public void setValue(Double value) {
        this.value = value;
    }

    public String getFilerRelation() {
        return filerRelation;
    }

    public void setFilerRelation(String filerRelation) {
        this.filerRelation = filerRelation;
    }

    public Integer getShares() {
        return shares;
    }

    public void setShares(Integer shares) {
        this.shares = shares;
    }

    public String getFilerUrl() {
        return filerUrl;
    }

    public void setFilerUrl(String filerUrl) {
        this.filerUrl = filerUrl;
    }

    public Integer getMaxAge() {
        return maxAge;
    }

    public void setMaxAge(Integer maxAge) {
        this.maxAge = maxAge;
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
        return id == that.id && Objects.equals(filerName, that.filerName) && Objects.equals(transactionText, that.transactionText) && Objects.equals(moneyText, that.moneyText) && Objects.equals(ownership, that.ownership) && Objects.equals(startDate, that.startDate) && Objects.equals(value, that.value) && Objects.equals(filerRelation, that.filerRelation) && Objects.equals(shares, that.shares) && Objects.equals(filerUrl, that.filerUrl) && Objects.equals(maxAge, that.maxAge);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, filerName, transactionText, moneyText, ownership, startDate, value, filerRelation, shares, filerUrl, maxAge);
    }
}
