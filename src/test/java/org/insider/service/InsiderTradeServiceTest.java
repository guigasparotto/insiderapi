package org.insider.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.insider.api.apiclient.ApiClient;
import org.insider.model.Transaction;
import org.insider.repository.JpaDatabaseManager;
import org.insider.repository.entities.SymbolsEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class InsiderTradeServiceTest {
    private InsiderTradeServiceImpl insiderTradeService;


    @Mock
    private ObjectMapper objectMapperMock;
    @Mock
    private ApiClient apiClientMock;
    @Mock
    private HttpResponse<String> responseMock;
    @Mock
    private JsonNode rootNodeMock;
    @Mock
    private JsonNode transactionNodeMock;
    @Mock
    private JsonNode nodeMock;
    @Mock
    private JpaDatabaseManager databaseManagerMock;

    @BeforeEach
    public void setUp() {
        ObjectMapper objectMapper = new ObjectMapper();
        insiderTradeService = new InsiderTradeServiceImpl(objectMapper, apiClientMock, databaseManagerMock);
    }

    @Test
    public void getInsiderTradingForSymbolTest() {
        String symbol = "ZOO.L";
        String region = "GB";
        SymbolsEntity symbolEntity = new SymbolsEntity(symbol, region, LocalDate.parse("2018-01-01"));

        // TODO: Does is make sense to use a real instance of ObjectMapper?
//        TransactionWrapper transactionWrapper =
//                new TransactionWrapper(List.of(transaction));

        when(databaseManagerMock.getSymbolRecord(symbol, region)).thenReturn(symbolEntity);
        when(apiClientMock.getInsiderTransactions(anyString(), anyString())).thenReturn(responseMock);
        when(responseMock.body()).thenReturn(getJsonYahooResponse());

        String response = insiderTradeService.getInsiderTradesForSymbol(
                "ZOO.L", "GB", "2019-01-01", "2019-01-01");

        assertEquals(getJsonExpectedResult(), response);
    }

    // TODO: Use the createTransaction method to build the transaction and generate the response
    // and expected result based on the same parameters
    private String getJsonYahooResponse() {
        return """
                {
                  "insiderTransactions": {
                    "transactions": [
                      {
                        "filerName":"Guilherme D",
                        "transactionText":"transaction text",
                        "moneyText":"money text",
                        "ownership":"D",
                        "startDate":{
                           "raw":1559001600,
                           "fmt":"2019-01-01"
                        },
                        "value":{
                           "raw":5000,
                           "fmt":"5000",
                           "longFmt":"5,000"
                        },
                        "filerRelation":"",
                        "shares":{
                           "raw":1000,
                           "fmt":null,
                           "longFmt":"0"
                        },
                        "filerUrl":"www.ggd.com",
                        "maxAge":1
                      }
                    ],
                    "maxAge": 1
                  }
                }""";
    }

    private String getJsonExpectedResult() {
        return "[{"
                + "\"filerName\":\"Guilherme D\","
                + "\"transactionText\":\"transaction text\","
                + "\"moneyText\":\"money text\","
                + "\"ownership\":\"D\","
                + "\"startDate\":\"2019-01-01\","
                + "\"value\":\"5000\","
                + "\"filerRelation\":\"\","
                + "\"shares\":\"1000\","
                + "\"filerUrl\":\"www.ggd.com\","
                + "\"maxAge\":1,"
                + "\"side\":\"not specified\","
                + "\"price\":" + null
                + "}]";
    }
}
