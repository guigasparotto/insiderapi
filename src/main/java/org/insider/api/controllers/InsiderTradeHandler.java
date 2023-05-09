package org.insider.api.controllers;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import jakarta.persistence.criteria.CriteriaBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.insider.service.InsiderTradeService;

import javax.management.QueryEval;
import java.io.IOException;
import java.io.OutputStream;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.Map;

public class InsiderTradeHandler implements HttpHandler {
    private static final Logger logger = LogManager.getLogger(InsiderTradeHandler.class);
    private final InsiderTradeService insiderTradeService;

    public InsiderTradeHandler(InsiderTradeService InsiderTradeService) {
        this.insiderTradeService = InsiderTradeService;
    }

    @Override
    public void handle(HttpExchange exchange) {
        try (exchange) {
            if (!exchange.getRequestMethod().equalsIgnoreCase("GET")) {
                String errorMessage = "Invalid request method: Only GET is allowed";
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

            try {
                validateParameters(symbolParam, regionParam, startDateParam, endDateParam);

                String jsonResponse =
                        insiderTradeService.getInsiderTradingForSymbol(
                                symbolParam.value(),
                                regionParam.value(),
                                startDateParam.value(),
                                endDateParam.value());

                // TODO: Find a better way to handle empty responses
                jsonResponse = !jsonResponse.equals("") ? jsonResponse : "No records found";

                exchange.getResponseHeaders().set("Content-Type", "application/json");
                exchange.sendResponseHeaders(200, jsonResponse.getBytes().length);

                OutputStream os = exchange.getResponseBody();
                os.write(jsonResponse.getBytes());
                os.close();
            } catch (IllegalArgumentException e) {
                String message = e.getMessage();

                logger.error(message);
                exchange.sendResponseHeaders(400, message.length());

                OutputStream errorOutput = exchange.getResponseBody();
                errorOutput.write(message.getBytes());
            }
        } catch (IOException e) {
            // TODO: Handle exceptions and send an appropriate error response
            logger.warn(e.getMessage());
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

        if (!isValidDate(startDate.value())) {
            throw new IllegalArgumentException("Invalid date format for startDate. Received " + startDate.value());
        } else if (!isValidDate(endDate.value())) {
            throw new IllegalArgumentException("Invalid date format for endDate. Received " + endDate.value());
        }
    }

    private boolean isValidDate(String dateStr) {
        try {
            LocalDate.parse(dateStr);
            return true;
        } catch (DateTimeParseException e) {
            logger.warn(e.getMessage());
            return false;
        }
    }

    private QueryParameter parseQueryParameter(String param) {
        String[] keyValue = param.split("=");
        return new QueryParameter(keyValue[0], keyValue[1]);
    }

    private record QueryParameter(String key, String value) {}
}
