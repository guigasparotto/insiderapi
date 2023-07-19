package org.insider.api.serialization;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.insider.model.Transaction;

import java.net.http.HttpResponse;
import java.util.List;

public interface ObjectMapperWrapper {
    void setUpModules();

    ObjectMapper getInstance();

    List<Transaction> deserializeTransactions(HttpResponse<String> httpResponse) throws JsonProcessingException;
}
