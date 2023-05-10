package org.insider.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.insider.api.apiclient.ApiClient;
import org.insider.model.Transaction;
import org.insider.repository.JpaDatabaseManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class InsiderTradeServiceTest {
    private InsiderTradeServiceImpl insiderTradeService;


    @Mock
    private ObjectMapper mockObjectMapper;
    @Mock
    private ApiClient mockApiClient;
    @Mock
    private HttpResponse<String> mockResponse;
    @Mock
    private JsonNode mockRootNode;
    @Mock
    private JsonNode mockTransactionsNode;
    @Mock
    private JsonNode mockNode;
    @Mock
    private JpaDatabaseManager databaseManager;

    @BeforeEach
    public void setUp() {
        ObjectMapper objectMapper = new ObjectMapper();
        insiderTradeService = new InsiderTradeServiceImpl(objectMapper, mockApiClient, databaseManager);
    }

    @Test
    public void getInsiderTradingForSymbolTest() throws JsonProcessingException {
        Transaction transaction = new Transaction(
                "Guilherme D",
                "transaction text",
                "money text",
                "D",
                new Transaction.LocalDateWrapper(LocalDate.of(2019, 1, 1)),
                "5000",
                "",
                "1000",
                "www.ggd.com",
                1,
                "buy",
                2.2);

        // TODO: Does is make sense to use a real instance of ObjectMapper?
//        TransactionWrapper transactionWrapper =
//                new TransactionWrapper(List.of(transaction));

        when(mockApiClient.getInsiderTransactions(anyString(), anyString())).thenReturn(mockResponse);
        when(mockResponse.body()).thenReturn(getJsonYahooResponse());
//        when(mockObjectMapper.readTree(mockResponse.body())).thenReturn(mockRootNode);
//        when(mockRootNode.path("insiderTransactions")).thenReturn(mockNode);
//        when(mockNode.path("transactions")).thenReturn(mockTransactionsNode);
//        when(mockTransactionsNode.toString()).thenReturn(getJsonExpectedResult());
//        when(mockObjectMapper.readValue(anyString(), eq(TransactionWrapper.class))).thenReturn(transactionWrapper);
//        when(mockObjectMapper.writeValueAsString(transactionWrapper))

        String response = String.valueOf(insiderTradeService.getInsiderTradesForSymbol(
                "ZOO.L", "GB", "2019-01-01", "2019-01-01"));

        assertEquals(getJsonExpectedResult(), response);
    }

    private Transaction createTransaction() {
        Transaction transaction;

        List<String> filerNames = List.of(
                "Random Traders UK Limited",
                "Hedge Masters LLC",
                "Uncle Scrooge Mc Duck",
                "Richie Rich",
                "Tony Stark Enterprises Limited"
        );

        String transactionText = "Bought at price 1.00 per share";

        return null;
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
