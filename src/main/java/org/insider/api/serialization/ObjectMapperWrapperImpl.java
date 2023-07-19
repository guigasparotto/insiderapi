package org.insider.api.serialization;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.insider.model.Transaction;

import java.net.http.HttpResponse;
import java.util.List;

public class ObjectMapperWrapperImpl implements ObjectMapperWrapper {
    private static final Logger logger = LogManager.getLogger(ObjectMapperWrapperImpl.class);
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public ObjectMapper getInstance() {
        return objectMapper;
    }

    @Override
    public void setUpModules() {
        SimpleModule module = new SimpleModule();
        objectMapper.registerModule(new JavaTimeModule());
        module.addDeserializer(TransactionWrapper.class, new TransactionWrapperDeserializer());
        objectMapper.registerModule(module);
    }

    @Override
    public List<Transaction> deserializeTransactions(HttpResponse<String> httpResponse)
            throws JsonProcessingException
    {
        JsonNode rootNode = objectMapper.readTree(httpResponse.body());
        JsonNode transactions = rootNode.path("insiderTransactions").path("transactions");

        TransactionWrapper transactionWrapper;
        transactionWrapper = objectMapper.readValue(transactions.toString(), TransactionWrapper.class);

        return transactionWrapper.transactions();
    }
}
