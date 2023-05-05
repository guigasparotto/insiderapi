package org.insider.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.insider.model.Transaction;
import org.insider.api.apiclient.ApiClient;
import org.insider.api.apiclient.YahooFinanceClient;
import org.insider.api.serialization.TransactionWrapper;
import org.insider.api.serialization.TransactionWrapperDeserializer;
import org.insider.repository.DatabaseManager;

import java.io.IOException;
import java.net.http.HttpResponse;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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
        List<Transaction> transactions =
                databaseManager.getByDateRange(symbol, region, startDate, endDate);

        if (!transactions.isEmpty()) {
            try {
                return objectMapper.writeValueAsString(transactions);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }

        String uri = YahooFinanceClient.INSIDERS_ENDPOINT
                + "?symbol=" + symbol
                + "&region=" + region;

        HttpResponse<String> response;

        try {
            response = apiClient.sendGetRequest(uri);
            JsonNode rootNode = objectMapper.readTree(response.body());
            JsonNode insiderTransactions =
                    rootNode.path("insiderTransactions").path("transactions");

            SimpleModule module = new SimpleModule();
            module.addDeserializer(TransactionWrapper.class, new TransactionWrapperDeserializer());
            objectMapper.registerModule(module);

            TransactionWrapper transactionWrapper = objectMapper.readValue(
                    insiderTransactions.toString(),
                    TransactionWrapper.class);

            transactions = transactionWrapper.getTransactions();

            // TODO: Save the transactions to the database

            databaseManager.saveToDatabase(transactions, symbol, region);

            return objectMapper.writeValueAsString(transactions);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
