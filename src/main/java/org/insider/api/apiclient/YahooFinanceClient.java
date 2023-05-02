package org.insider.api.apiclient;

import org.insider.util.ApplicationProperties;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class YahooFinanceClient implements ApiClient {
    private static final String YAHOO_URL = "https://yh-finance.p.rapidapi.com/stock/v2";
    public static final String INSIDERS_ENDPOINT = "/get-insider-transactions";
    private final ApplicationProperties properties;

    public YahooFinanceClient(ApplicationProperties properties) {
        this.properties = properties;
    }

    @Override
    public HttpResponse<String> sendGetRequest(String uri) {
        HttpRequest request = createGetRequest(YAHOO_URL + uri);
        HttpResponse<String> response;

        try {
            response = HttpClient.newHttpClient().
                    send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }

        return response;
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
