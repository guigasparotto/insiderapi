package org.insider.api.apiclient;

import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class YahooFinanceClientTest {
    private YahooFinanceClient yahooClient;

    // Default values
    private final String symbol = "TEST.L";
    private final String region = "GB";

    @Mock
    private HttpClient httpClientMock;
    @Mock
    private HttpResponse<String> httpResponseMock;

    @BeforeEach
    public void setUp() {
        yahooClient = new YahooFinanceClient(httpClientMock);
    }

    @Test
    public void getInsiderTransactions_successfulRequest_returnsHttpResponse() throws IOException, InterruptedException {
        when(httpClientMock.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(httpResponseMock);
        String expectedResponse = getJsonYahooResponse();
        when(httpResponseMock.body()).thenReturn(expectedResponse);

        HttpResponse<String> response = yahooClient.getInsiderTransactions(symbol, region);

        assertEquals(expectedResponse, response.body());
    }

    @Test
    public void getInsiderTransactions_failedRequest_throwsRuntimeException()
            throws IOException, InterruptedException
    {
        String uri = YahooFinanceClient.INSIDERS_ENDPOINT + "?symbol=" + symbol + "&region=" + region;
        String url = YahooFinanceClient.YAHOO_URL + uri;
        String expectedMessage = "Error occurred while sending GET request to " + url;

        IOException ioException = new IOException("Internal exception message");
        when(httpClientMock.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenThrow(ioException);

        RuntimeException actualException = assertThrows(RuntimeException.class,
                () -> yahooClient.getInsiderTransactions(symbol, region));

        assertEquals(expectedMessage, actualException.getMessage());
        assertEquals(ioException, actualException.getCause());
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
}
