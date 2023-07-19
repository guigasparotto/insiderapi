package org.insider.service;

import com.fasterxml.jackson.core.JsonProcessingException;

public interface InsiderTradeService {

    String getInsiderTradesForSymbol(
            String symbol, String region, String startDate, String endDate) throws JsonProcessingException;
}
