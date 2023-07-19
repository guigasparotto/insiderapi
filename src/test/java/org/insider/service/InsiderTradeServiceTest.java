package org.insider.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.insider.api.apiclient.ApiClient;
import org.insider.api.serialization.ObjectMapperWrapper;
import org.insider.api.serialization.ObjectMapperWrapperImpl;
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
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class InsiderTradeServiceTest {
    private InsiderTradeServiceImpl insiderTradeService;

    @Mock
    private ObjectMapper objectMapperMock;
    @Mock
    private ObjectMapperWrapper objectMapperWrapperMock;
    @Mock
    private ApiClient apiClientMock;
    @Mock
    private HttpResponse<String> responseMock;
    @Mock
    private JpaDatabaseManager databaseManagerMock;

	// Default values
    private final String symbol = "TEST.L";
    private final String region = "GB";
    String startDate = "2019-01-01";
    String endDate = "2020-01-01";

    @BeforeEach
    public void setUp() {
        insiderTradeService = new InsiderTradeServiceImpl(
                objectMapperWrapperMock, apiClientMock, databaseManagerMock);
    }

    @Test
    public void getInsiderTrades_existingSymbol_callsExternalApi_returnsJSONResponse() throws JsonProcessingException {
        // Symbol exists in the database with an old date, so an external API call will be made
        SymbolsEntity symbolEntity = new SymbolsEntity(symbol, region, LocalDate.parse("2018-01-01"));

        when(databaseManagerMock.getSymbolRecord(symbol, region)).thenReturn(Optional.of(symbolEntity));
        when(apiClientMock.getInsiderTransactions(anyString(), anyString())).thenReturn(responseMock);

        Transaction expectedTransaction = getExpectedTransaction();
        when(objectMapperWrapperMock.deserializeTransactions(any(HttpResponse.class)))
                .thenReturn(List.of(expectedTransaction));
        when(objectMapperWrapperMock.getInstance()).thenReturn(objectMapperMock);
        when(objectMapperMock.writeValueAsString(List.of(expectedTransaction)))
                .thenReturn(getJsonExpectedResult());

        String response = insiderTradeService.getInsiderTradesForSymbol(
                symbol, region, startDate, endDate);

        assertEquals(getJsonExpectedResult(), response);
        verify(databaseManagerMock).getSymbolRecord(symbol, region);
        verify(apiClientMock).getInsiderTransactions(anyString(), anyString());
        verify(objectMapperWrapperMock).deserializeTransactions(any(HttpResponse.class));
    }

    @Test
    public void getInsiderTrades_existingSymbol_retrievesFromDb_returnsJSONResponse() throws JsonProcessingException {
        // Symbol exists in the database with today's date, so no API calls are made,
        // transactions are queried from the database
        SymbolsEntity symbolEntity = new SymbolsEntity(symbol, region, LocalDate.now());

        Transaction expectedTransaction = getExpectedTransaction();
        when(databaseManagerMock.getSymbolRecord(symbol, region)).thenReturn(Optional.of(symbolEntity));
        when(databaseManagerMock.getTransactionsByRange(symbol, region, startDate, endDate))
                .thenReturn(List.of(expectedTransaction));

        when(objectMapperWrapperMock.getInstance()).thenReturn(objectMapperMock);
        when(objectMapperMock.writeValueAsString(List.of(expectedTransaction)))
                .thenReturn(getJsonExpectedResult());

        String response = insiderTradeService.getInsiderTradesForSymbol(
                symbol, region, startDate, endDate);

        assertEquals(getJsonExpectedResult(), response);
        verify(databaseManagerMock).getSymbolRecord(symbol, region);
        verify(databaseManagerMock).getTransactionsByRange(symbol, region, startDate, endDate);
    }

    // TODO: How useful is this test, given it just tests if the exception is replicated?
    @Test
    public void getInsiderTradingForSymbol_invalidRequest_throwsException() throws JsonProcessingException {
        when(databaseManagerMock.getSymbolRecord(symbol, region)).thenReturn(Optional.empty());
        when(apiClientMock.getInsiderTransactions(anyString(), anyString())).thenReturn(responseMock);
        when(objectMapperWrapperMock.deserializeTransactions(any(HttpResponse.class)))
                .thenThrow(new JsonProcessingException("Deserialization error") {});

        Exception e = assertThrows(JsonProcessingException.class, () ->
                insiderTradeService.getInsiderTradesForSymbol(symbol, region, startDate, endDate));

        verify(databaseManagerMock).getSymbolRecord(symbol, region);
        assertEquals("Deserialization error", e.getMessage());
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

    private Transaction getExpectedTransaction() {
        return new Transaction(
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
                "not specified",
                null
        );
    }
}
