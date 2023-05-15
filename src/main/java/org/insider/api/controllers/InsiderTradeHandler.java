package org.insider.api.controllers;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.insider.service.InsiderTradeService;

import java.io.IOException;
import java.io.OutputStream;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

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

            Map<String, String> parameterMap = parseParameters(exchange);

            String symbol = parameterMap.get("symbol");
            String region = parameterMap.get("region");
            String startDate = parameterMap.get("startDate");
            String endDate = parameterMap.get("endDate");

            try {
                validateParameters(parameterMap);

                String jsonResponse =
                        insiderTradeService.getInsiderTradesForSymbol(symbol, region, startDate, endDate);

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
        exchange.sendResponseHeaders(statusCode, message.getBytes().length);

        try (OutputStream os = exchange.getResponseBody()) {
            os.write(message.getBytes());
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
    }

    private void validateParameters(Map<String, String> parameterMap) throws IllegalArgumentException {
        if (!parameterMap.containsKey("symbol")
                || !parameterMap.containsKey("region")
                || !parameterMap.containsKey("startDate")
                || !parameterMap.containsKey("endDate"))
        {
            Set<String> keys = parameterMap.keySet();

            StringBuilder messageBuilder = new StringBuilder("""
                    Unexpected parameter key received.
                    Expected: symbol, region, startDate and endDate
                    Received:\s""");

            for (String key : keys) {
                messageBuilder.append(key).append(", ");
            }

            throw new IllegalArgumentException(messageBuilder.toString());
        }

        String startDateValue = parameterMap.get("startDate");
        String endDateValue = parameterMap.get("endDate");

        if (isInvalidDate(startDateValue)) {
            throw new IllegalArgumentException("Invalid date format for startDate. Received " + startDateValue);
        } else if (isInvalidDate(endDateValue)) {
            throw new IllegalArgumentException("Invalid date format for endDate. Received " + endDateValue);
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

    private Map<String, String> parseParameters(HttpExchange exchange) {
        Map<String, String> parameterMap = new HashMap<>();

        String query = exchange.getRequestURI().getQuery();
        String[] queryParameters = query.split("&");

        for (String parameter : queryParameters) {
            String[] keyValue = parameter.split("=");
            parameterMap.put(keyValue[0], keyValue[1]);
        }

        return parameterMap;
    }
}
