package org.insider.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.insider.api.apiclient.ApiClient;
import org.insider.api.apiclient.YahooFinanceClient;
import org.insider.api.serialization.TransactionWrapper;
import org.insider.api.serialization.TransactionWrapperDeserializer;
import org.insider.model.Transaction;
import org.insider.repository.DatabaseManager;
import org.insider.repository.SymbolsEntity;

import java.net.http.HttpResponse;
import java.sql.Date;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static java.time.LocalDate.now;
import static java.time.LocalDate.parse;

public class InsiderTradeService {
    private static final Logger logger = LogManager.getLogger(InsiderTradeService.class);
    private final ObjectMapper objectMapper;
    private final ApiClient apiClient;
    private final DatabaseManager databaseManager;


    public InsiderTradeService(ObjectMapper objectMapper, ApiClient apiClient, DatabaseManager databaseManager) {
        this.objectMapper = objectMapper;
        this.objectMapper.registerModule(new JavaTimeModule());
        this.apiClient = apiClient;
        this.databaseManager = databaseManager;
    }

    public String getInsiderTradingForSymbol(
            String symbol, String region, String startDate, String endDate)
    {
        List<Transaction> queryResult = null;
        Date updated = Optional.ofNullable(databaseManager.getSymbolRecord(symbol, region))
                .map(SymbolsEntity::getUpdated)
                .orElse(null);

        if (updated != null && updated.compareTo(Date.valueOf(now())) == 0) {
            queryResult = databaseManager.getTransactionsByRange(symbol, region, startDate, endDate);
        }

        try {
            if (queryResult == null) {
                String uri = YahooFinanceClient.INSIDERS_ENDPOINT
                        + "?symbol=" + symbol
                        + "&region=" + region;

                HttpResponse<String> httpResponse;

                httpResponse = apiClient.sendGetRequest(uri);
                JsonNode rootNode = objectMapper.readTree(httpResponse.body());
                JsonNode insiderTransactions =
                        rootNode.path("insiderTransactions").path("transactions");

                SimpleModule module = new SimpleModule();
                module.addDeserializer(TransactionWrapper.class, new TransactionWrapperDeserializer());
                objectMapper.registerModule(module);

                TransactionWrapper transactionWrapper = objectMapper.readValue(
                        insiderTransactions.toString(),
                        TransactionWrapper.class);

                List<Transaction> transactions = transactionWrapper.getTransactions();
                boolean success = databaseManager.saveToDatabase(transactions, symbol, region);

                if (!success) {
                    logger.error("Failed to save transaction list to the database");
                }

                queryResult = parseList(transactions, startDate, endDate);
            }

            return objectMapper.writeValueAsString(queryResult);
        } catch (JsonProcessingException e) {
            logger.error(e.getMessage());
        }

        return "[]";
    }

    private List<Transaction> parseList(List<Transaction> transactions, String startDate, String endDate) {
        List<Transaction> parsedResult = new ArrayList<>();

        for (var t : transactions) {
            try {
                if ((parse(t.getStartDate()).compareTo(parse(startDate)) >= 0)
                        && (parse(t.getStartDate()).compareTo(parse(endDate)) <= 0)) {
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
