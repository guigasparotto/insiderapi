package org.insider.api.apiclient;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.insider.util.ConfigReader;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class YahooFinanceClient implements ApiClient {
    private static final Logger logger = LogManager.getLogger(ApiClient.class);
    public static final String YAHOO_URL = "https://apidojo-yahoo-finance-v1.p.rapidapi.com/stock/v2";
    public static final String INSIDERS_ENDPOINT = "/get-insider-transactions";
    private final HttpClient httpClient;

    public YahooFinanceClient(HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    @Override
    public HttpResponse<String> getInsiderTransactions(String symbol, String region) {
        String uri = getUri(INSIDERS_ENDPOINT, "?symbol=" + symbol, "&region=" + region);

        HttpRequest request = createGetRequest(YAHOO_URL + uri);
        HttpResponse<String> response;

        try {
            response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            logger.info("GET request successfully sent to " + request.uri());
        } catch (IOException | InterruptedException e) {
            String message = "Error occurred while sending GET request to " + request.uri();
            logger.error(message, e);
            throw new RuntimeException(message, e);
        }

        return response;
    }

    public String getUri(String endpoint, String ...params) {
        StringBuilder uri = new StringBuilder(endpoint);

        for (String param : params) {
            uri.append(param);
        }

        return uri.toString();
    }

    private HttpRequest createGetRequest(String uri) {
        return HttpRequest.newBuilder()
                .uri(URI.create(uri))
                .header("content-type", "application/json")
                .header("X-RapidAPI-Key", ConfigReader.getProperty("api-key"))
                .header("X-RapidAPI-Host", "apidojo-yahoo-finance-v1.p.rapidapi.com")
                .method("GET", HttpRequest.BodyPublishers.noBody())
                .build();
    }
}
