package org.insider.repository;

import jakarta.persistence.*;

import java.sql.Date;
import java.util.Objects;

@Entity
@Table(name = "symbols", schema = "public", catalog = "insider_trade")
public class SymbolsEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(nullable = false)
    private String symbol;
    @Column(nullable = false)
    private String region;
    @Column(nullable = false)
    private Date updated;

    public int getId() {
        return id;
    }

    // Hibernate requires a default constructor with at least protected visibility
    protected  SymbolsEntity() {
    }

    public SymbolsEntity(String symbol, String region, Date updated) {
        this.symbol = Objects.requireNonNull(symbol);
        this.region = Objects.requireNonNull(region);
        this.updated = Objects.requireNonNull(updated);
    }

    public String getSymbol() {
        return symbol;
    }

    public String getRegion() {
        return region;
    }

    public Date getUpdated() {
        return updated;
    }

    public void setUpdated(Date updated) {
        this.updated = Objects.requireNonNull(updated);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SymbolsEntity that = (SymbolsEntity) o;
        return id == that.id
               && Objects.equals(symbol, that.symbol)
               && Objects.equals(region, that.region)
               && Objects.equals(updated, that.updated);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, symbol, region, updated);
    }
}
