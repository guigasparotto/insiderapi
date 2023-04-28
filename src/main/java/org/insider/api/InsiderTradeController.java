package org.insider.api;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.insider.model.Transaction;
import org.insider.service.InsiderTradeService;
import java.io.OutputStream;
import java.util.List;

public class InsiderTradeController {
    private final InsiderTradeService insiderTradeService;

    public InsiderTradeController(InsiderTradeService InsiderTradeService) {
        this.insiderTradeService = InsiderTradeService;
    }

    public void registerEndpoints(HttpServer server) {
        server.createContext("/insiders", new HttpHandler() {
            @Override
            public void handle(HttpExchange exchange) {
                try (exchange) {
                    String query = exchange.getRequestURI().getQuery();
                    String[] symbolRegion = query.split("&");

                    String[] symbolKeyValue = symbolRegion[0].split("=");
                    String symbolKey = symbolKeyValue[0];
                    String symbolValue = symbolKeyValue[1];

                    String[] regionKeyValue = symbolRegion[1].split("=");
                    String regionKey = regionKeyValue[0];
                    String regionValue = regionKeyValue[1];

                    if (!symbolKey.equals("symbol") || !regionKey.equals("region")) {
                        exchange.sendResponseHeaders(400, 0);
                        return;
                    }

                    String jsonResponse =
                            insiderTradeService.getInsiderTradingForSymbol(symbolValue, regionValue);

                    exchange.getResponseHeaders().set("Content-Type", "application/json");
                    exchange.sendResponseHeaders(200, jsonResponse.getBytes().length);
                    OutputStream os = exchange.getResponseBody();
                    os.write(jsonResponse.getBytes());
                    os.close();
                } catch (Exception e) {
                    // TODO: Handle exceptions and send an appropriate error response
                    System.out.println(e.getMessage());
                }
            }
        });
    }
}
