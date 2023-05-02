package org.insider;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpServer;
import org.insider.api.controllers.InsiderTradeHandler;
import org.insider.service.InsiderTradeService;
import org.insider.api.apiclient.ApiClient;
import org.insider.util.ApplicationProperties;
import org.insider.api.apiclient.YahooFinanceClient;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class Application {
    private static final int NUMBER_OF_THREADS = 1;
    public static void main(String[] args) throws IOException {
        ApplicationProperties properties = new ApplicationProperties();
        ObjectMapper objectMapper = new ObjectMapper();
        ApiClient apiClient = new YahooFinanceClient(properties);

        InsiderTradeService insiderTradeService = new InsiderTradeService(objectMapper, apiClient);
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
        server.createContext("/insiders", new InsiderTradeHandler(insiderTradeService));

        Executor executor = Executors.newFixedThreadPool(NUMBER_OF_THREADS);
        server.setExecutor(executor);
        server.start();
    }
}