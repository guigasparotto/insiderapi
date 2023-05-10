package org.insider.api.apiclient;

import java.net.http.HttpResponse;

public interface ApiClient {
    HttpResponse<String> getInsiderTransactions(String symbol, String region);
}
