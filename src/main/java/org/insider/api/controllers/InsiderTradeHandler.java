package org.insider.api.controllers;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.insider.service.InsiderTradeService;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;

public class InsiderTradeHandler implements HttpHandler {
    private final InsiderTradeService insiderTradeService;

    public InsiderTradeHandler(InsiderTradeService InsiderTradeService) {
        this.insiderTradeService = InsiderTradeService;
    }

    @Override
    public void handle(HttpExchange exchange) {
        try (exchange) {
            if (!exchange.getRequestMethod().equalsIgnoreCase("GET")) {
                String errorMessage = "Invalid request method: Only GET is allowed";
                exchange.getResponseHeaders().set("Content-Type", "text/plain");
                exchange.sendResponseHeaders(405, errorMessage.length());

                OutputStream errorOutput = exchange.getResponseBody();
                errorOutput.write(errorMessage.getBytes());
            }

            String query = exchange.getRequestURI().getQuery();
            String[] queryParameters = query.split("&");

            QueryParameter symbolParam = parseQueryParameter(queryParameters[0]);
            QueryParameter regionParam = parseQueryParameter(queryParameters[1]);
            QueryParameter startDateParam = parseQueryParameter(queryParameters[2]);
            QueryParameter endDateParam = parseQueryParameter(queryParameters[3]);

            if (!symbolParam.key().equals("symbol")
                || !regionParam.key().equals("region")
                || !startDateParam.key().equals("startDate")
                || !endDateParam.key().equals("endDate"))
            {
                exchange.sendResponseHeaders(400, 0);
                return;
            }

            String jsonResponse =
                    insiderTradeService.getInsiderTradingForSymbol(
                            symbolParam.value(),
                            regionParam.value(),
                            startDateParam.value(),
                            endDateParam.value());

            // TODO: Find a better way to handle empty responses
            jsonResponse = jsonResponse != null ? jsonResponse : "No records found";

            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, jsonResponse.getBytes().length);

            OutputStream os = exchange.getResponseBody();
            os.write(jsonResponse.getBytes());
            os.close();
        } catch (IOException e) {
            // TODO: Handle exceptions and send an appropriate error response
            System.out.println(e.getMessage());
        }
    }

    private QueryParameter parseQueryParameter(String param) {
        String[] keyValue = param.split("=");
        return new QueryParameter(keyValue[0], keyValue[1]);
    }

    private record QueryParameter(String key, String value) {}
}
