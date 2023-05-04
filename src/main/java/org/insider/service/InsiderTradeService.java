package org.insider.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.insider.model.Transaction;
import org.insider.api.apiclient.ApiClient;
import org.insider.api.apiclient.YahooFinanceClient;
import org.insider.api.serialization.TransactionWrapper;
import org.insider.api.serialization.TransactionWrapperDeserializer;
import org.insider.repository.DatabaseWrapper;

import java.io.IOException;
import java.net.http.HttpResponse;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.xml.crypto.Data;

public class InsiderTradeService {
    private static final Logger logger = LogManager.getLogger(InsiderTradeService.class);
    private final ObjectMapper objectMapper;
    private final ApiClient apiClient;
    private final DatabaseWrapper databaseWrapper;


    public InsiderTradeService(ObjectMapper objectMapper, ApiClient apiClient, DatabaseWrapper databaseWrapper) {
        this.objectMapper = objectMapper;
        this.objectMapper.registerModule(new JavaTimeModule());
        this.apiClient = apiClient;
        this.databaseWrapper = databaseWrapper;
    }

    public String getInsiderTradingForSymbol(String symbol, String region) {
        // TODO: First verify if the transactions for this symbol exist in the database
        // if not, then we need to request it from external API.
        // Ideally this method should receive a list of symbols in the body of the request,
        // containing symbol, region, initial and end date
        // this information can be used to query the database

        String uri = YahooFinanceClient.INSIDERS_ENDPOINT
                + "?symbol=" + symbol
                + "&region=" + region;

        HttpResponse<String> response;
        List<Transaction> transactions;

        try {
            response = apiClient.sendGetRequest(uri);
            JsonNode rootNode = objectMapper.readTree(response.body());
            JsonNode insiderTransactions = rootNode.path("insiderTransactions").path("transactions");

            SimpleModule module = new SimpleModule();
            module.addDeserializer(TransactionWrapper.class, new TransactionWrapperDeserializer());
            objectMapper.registerModule(module);

            TransactionWrapper transactionWrapper = objectMapper.readValue(
                    insiderTransactions.toString(),
                    TransactionWrapper.class);

            transactions = transactionWrapper.getTransactions();

            // TODO: Save the transactions to the database

            databaseWrapper.saveToDatabase(transactions);

            return objectMapper.writeValueAsString(transactions);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
