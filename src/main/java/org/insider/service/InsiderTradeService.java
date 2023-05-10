package org.insider.service;

public interface InsiderTradeService {
    String getInsiderTradesForSymbol(
            String symbol, String region, String startDate, String endDate);
}
