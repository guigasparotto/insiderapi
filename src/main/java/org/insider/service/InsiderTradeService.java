package org.insider.service;

import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.insider.model.Transaction;
import org.insider.util.ApplicationProperties;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.insider.util.TransactionDeserializer;
import org.insider.util.TransactionWrapper;
import org.insider.util.TransactionWrapperDeserializer;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

public class InsiderTradeService {
    private static final String YAHOO_FINANCE_URL = "https://yh-finance.p.rapidapi.com/stock/v2";
    private static final String INSIDERS_ENDPOINT = "/get-insider-transactions";
    private final ApplicationProperties properties;
    private final ObjectMapper objectMapper;


    public InsiderTradeService(ApplicationProperties properties, ObjectMapper objectMapper) {
        this.properties = properties;
        this.objectMapper = objectMapper;
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    public String getInsiderTradingForSymbol(String symbol, String region) {
        // TODO: First verify if the transactions for this symbol exist in the database
        // if not, then we need to request it from external API.
        // Ideally this method should receive a list of symbols in the body of the request,
        // containing symbol, region, initial and end date
        // this information can be used to query the database

        String uri = YAHOO_FINANCE_URL
                + INSIDERS_ENDPOINT
                + "?symbol=" + symbol
                + "&region=" + region;

        HttpRequest request = createGetRequest(uri);
        HttpResponse<String> response;

        List<Transaction> transactions;

        try {
            response = HttpClient.newHttpClient().
                    send(request, HttpResponse.BodyHandlers.ofString());

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

            return objectMapper.writeValueAsString(transactions);
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private HttpRequest createGetRequest(String uri) {
        return HttpRequest.newBuilder()
                .uri(URI.create(uri))
                .header("content-type", "application/json")
                .header("X-RapidAPI-Key", properties.getProperty("api-key"))
                .header("X-RapidAPI-Host", "yh-finance.p.rapidapi.com")
                .method("GET", HttpRequest.BodyPublishers.noBody())
                .build();
    }
}
