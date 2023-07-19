package org.insider.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.insider.api.apiclient.ApiClient;
import org.insider.api.serialization.ObjectMapperWrapper;
import org.insider.model.Transaction;
import org.insider.repository.DatabaseManager;
import org.insider.repository.entities.SymbolsEntity;

import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static java.time.LocalDate.parse;

public class InsiderTradeServiceImpl implements InsiderTradeService {
    private static final Logger logger = LogManager.getLogger(InsiderTradeServiceImpl.class);
    private final ObjectMapperWrapper objectMapperWrapper;
    private final ApiClient apiClient;
    private final DatabaseManager databaseManager;


    public InsiderTradeServiceImpl(
            ObjectMapperWrapper objectMapperWrapper,
            ApiClient apiClient,
            DatabaseManager databaseManager)
    {
        this.objectMapperWrapper = objectMapperWrapper;
        this.objectMapperWrapper.setUpModules();
        this.apiClient = apiClient;
        this.databaseManager = databaseManager;
    }

    @Override
    public String getInsiderTradesForSymbol(
            String symbol, String region, String startDate, String endDate) throws JsonProcessingException
    {
        LocalDate parsedStart = parse(startDate);
        LocalDate parsedEnd = parse(endDate);
        List<Transaction> queryResult = null;

        Optional<LocalDate> updated =
                databaseManager.getSymbolRecord(symbol, region)
                .map(SymbolsEntity::getUpdated);

        if (updated.isPresent() && updated.get().isEqual(LocalDate.now())) {
            queryResult = databaseManager.getTransactionsByRange(symbol, region, startDate, endDate);
        }

        if (queryResult == null) {
            HttpResponse<String> httpResponse = apiClient.getInsiderTransactions(symbol, region);
            List<Transaction> transactions = objectMapperWrapper.deserializeTransactions(httpResponse);
            databaseManager.saveTransactions(transactions, symbol, region);
            queryResult = parseList(transactions, parsedStart, parsedEnd);
        }

        return objectMapperWrapper.getInstance().writeValueAsString(queryResult);
    }

    private List<Transaction> parseList(List<Transaction> transactions, LocalDate start, LocalDate end) {
        List<Transaction> parsedResult = new ArrayList<>();

        for (var t : transactions) {
            try {
                if ((!parse(t.getStartDate()).isBefore(start))
                        && (!parse(t.getStartDate()).isAfter(end))) {
                    parsedResult.add(t);
                }
            } catch (DateTimeParseException e) {
                logger.error("Failed to parse record: " + t);
                logger.error(e.getMessage());
            }
        }

        return parsedResult;
    }
}
