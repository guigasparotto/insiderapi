package org.insider;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpServer;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import org.flywaydb.core.Flyway;
import org.insider.api.controllers.InsiderTradeHandler;
import org.insider.repository.DatabaseManager;
import org.insider.repository.JpaDatabaseManager;
import org.insider.service.InsiderTradeService;
import org.insider.service.InsiderTradeServiceImpl;
import org.insider.api.apiclient.ApiClient;
import org.insider.api.apiclient.YahooFinanceClient;
import org.insider.util.ConfigReader;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class Application {
    private static final int NUMBER_OF_THREADS = 1;
    public static void main(String[] args) throws IOException {
        // Run database migration
        Flyway flyway = Flyway.configure().dataSource(
                ConfigReader.getProperty("database.url"),
                ConfigReader.getProperty("database.username"),
                ConfigReader.getProperty("database.password")
        ).load();

        flyway.migrate();

        // Create the objects to be injected into services
        ObjectMapper objectMapper = new ObjectMapper();
        ApiClient apiClient = new YahooFinanceClient();

        EntityManagerFactory entityManagerFactory =
                Persistence.createEntityManagerFactory("default",
                        new HashMap<String, String>() {
                            {
                                put("hibernate.connection.url", ConfigReader.getProperty("database.url"));
                                put("hibernate.connection.username", ConfigReader.getProperty("database.username"));
                                put("hibernate.connection.password", ConfigReader.getProperty("database.password"));
                            }
                        });

        DatabaseManager databaseManager = new JpaDatabaseManager(entityManagerFactory);

        // Start services, set number of threads for the http server and start it
        InsiderTradeService insiderTradeService = new InsiderTradeServiceImpl(objectMapper, apiClient, databaseManager);
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
        server.createContext("/insiders", new InsiderTradeHandler(insiderTradeService));

        Executor executor = Executors.newFixedThreadPool(NUMBER_OF_THREADS);
        server.setExecutor(executor);
        server.start();

        // Add a shutdown hook to close the EntityManagerFactory
        Runtime.getRuntime().addShutdownHook(new Thread(databaseManager::closeEntityManagerFactory));
    }
}