package org.insider;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpServer;
import org.insider.api.controllers.InsiderTradeHandler;
import org.insider.repository.DatabaseWrapper;
import org.insider.service.InsiderTradeService;
import org.insider.api.apiclient.ApiClient;
import org.insider.api.apiclient.YahooFinanceClient;

import javax.xml.crypto.Data;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class Application {
    private static final int NUMBER_OF_THREADS = 1;
    public static void main(String[] args) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        ApiClient apiClient = new YahooFinanceClient();
        DatabaseWrapper databaseWrapper = new DatabaseWrapper();

        InsiderTradeService insiderTradeService = new InsiderTradeService(objectMapper, apiClient, databaseWrapper);
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
        server.createContext("/insiders", new InsiderTradeHandler(insiderTradeService));

        Executor executor = Executors.newFixedThreadPool(NUMBER_OF_THREADS);
        server.setExecutor(executor);
        server.start();

        // Add a shutdown hook to close the EntityManagerFactory
        Runtime.getRuntime().addShutdownHook(new Thread(databaseWrapper::closeEntityManagerFactory));
    }
}