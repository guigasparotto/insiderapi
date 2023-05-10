package org.insider.api.controllers;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.insider.service.InsiderTradeService;
import org.insider.service.InsiderTradeServiceImpl;

import java.io.IOException;
import java.io.OutputStream;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

public class InsiderTradeHandler implements HttpHandler {
    private static final Logger logger = LogManager.getLogger(InsiderTradeHandler.class);
    private final InsiderTradeService insiderTradeService;

    public InsiderTradeHandler(InsiderTradeService insiderTradeService) {
        this.insiderTradeService = insiderTradeService;
    }

    @Override
    public void handle(HttpExchange exchange) {
        try (exchange) {
            if (!exchange.getRequestMethod().equalsIgnoreCase("GET")) {
                sendResponse(exchange, 405, "Invalid request method: Only GET is allowed");
            }

            String query = exchange.getRequestURI().getQuery();
            String[] queryParameters = query.split("&");

            // TODO: Fix-me - Index out of bounds when the number of parameters is incorrect - Use a map?
            QueryParameter symbolParam = parseQueryParameter(queryParameters[0]);
            QueryParameter regionParam = parseQueryParameter(queryParameters[1]);
            QueryParameter startDateParam = parseQueryParameter(queryParameters[2]);
            QueryParameter endDateParam = parseQueryParameter(queryParameters[3]);

            try {
                validateParameters(symbolParam, regionParam, startDateParam, endDateParam);

                String jsonResponse =
                        insiderTradeService.getInsiderTradesForSymbol(
                                symbolParam.value(),
                                regionParam.value(),
                                startDateParam.value(),
                                endDateParam.value());

                // TODO: Find a better way to handle empty responses
                jsonResponse = !jsonResponse.equals("") ? jsonResponse : "No records found";

                sendResponse(exchange, 200, jsonResponse);
            } catch (IllegalArgumentException e) {
                String message = e.getMessage();
                logger.error(message);

                sendResponse(exchange, 400, message);
            }
        } catch (IOException e) {
            // TODO: Handle exceptions and send an appropriate error response
            logger.error(e.getMessage());
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
    }

    private void sendResponse(HttpExchange exchange, int statusCode, String message) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(statusCode, message.length());

        try (OutputStream os = exchange.getResponseBody()) {
            os.write(message.getBytes());
        }
    }

    private void validateParameters(
            QueryParameter symbol,
            QueryParameter region,
            QueryParameter startDate,
            QueryParameter endDate) throws IllegalArgumentException
    {
        if (!symbol.key().equals("symbol")
                || !region.key().equals("region")
                || !startDate.key().equals("startDate")
                || !endDate.key().equals("endDate"))
        {
            throw new IllegalArgumentException("Unexpected parameter key received." +
                    "\nExpected: symbol, region, startDate and endDate" +
                    "\nReceived: " + symbol.key() + ", " + region.key() + ", "
                    + startDate.key() + " and " + endDate.key());
        }

        if (isInvalidDate(startDate.value())) {
            throw new IllegalArgumentException("Invalid date format for startDate. Received " + startDate.value());
        } else if (isInvalidDate(endDate.value())) {
            throw new IllegalArgumentException("Invalid date format for endDate. Received " + endDate.value());
        }
    }

    private boolean isInvalidDate(String dateStr) {
        try {
            LocalDate.parse(dateStr);
            return false;
        } catch (DateTimeParseException e) {
            logger.warn(e.getMessage());
            return true;
        }
    }

    private QueryParameter parseQueryParameter(String param) {
        String[] keyValue = param.split("=");
        return new QueryParameter(keyValue[0], keyValue[1]);
    }

    private record QueryParameter(String key, String value) {}
}
