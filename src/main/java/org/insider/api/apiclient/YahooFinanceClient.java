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
    private static final String YAHOO_URL = "https://apidojo-yahoo-finance-v1.p.rapidapi.com/stock/v2";
    public static final String INSIDERS_ENDPOINT = "/get-insider-transactions";

    public YahooFinanceClient() {
    }

    @Override
    public HttpResponse<String> getInsiderTransactions(String symbol, String region) {
        String uri = YahooFinanceClient.INSIDERS_ENDPOINT
                + "?symbol=" + symbol
                + "&region=" + region;

        HttpRequest request = createGetRequest(YAHOO_URL + uri);
        HttpResponse<String> response;

        try {
            response = HttpClient.newHttpClient().
                    send(request, HttpResponse.BodyHandlers.ofString());
            logger.info("GET request successfully sent to " + request.uri());
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }

        return response;
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
