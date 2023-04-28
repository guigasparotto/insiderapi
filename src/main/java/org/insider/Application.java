package org.insider;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpServer;
import org.insider.api.InsiderTradeController;
import org.insider.service.InsiderTradeService;
import org.insider.util.ApplicationProperties;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class Application {
    private static final int NUMBER_OF_THREADS = 1;
    public static void main(String[] args) throws IOException {
        ApplicationProperties properties = new ApplicationProperties();
        ObjectMapper objectMapper = new ObjectMapper();

        InsiderTradeService insiderTradeService = new InsiderTradeService(properties, objectMapper);
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
        InsiderTradeController insiderTradeController = new InsiderTradeController(insiderTradeService);
        insiderTradeController.registerEndpoints(server);

        Executor executor = Executors.newFixedThreadPool(NUMBER_OF_THREADS);
        server.setExecutor(executor);
        server.start();
    }
}