package org.insider.util.apiclient;

import java.net.http.HttpResponse;

public interface ApiClient {
    HttpResponse<String> sendGetRequest(String uri);
}
