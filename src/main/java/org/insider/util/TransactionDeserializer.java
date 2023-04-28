package org.insider.util;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import org.insider.model.Transaction;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Optional;

public class TransactionDeserializer extends StdDeserializer<Transaction> {
    public TransactionDeserializer() {
        super(Transaction.class);
    }

    @Override
    public Transaction deserialize(
            JsonParser parser,
            DeserializationContext context) throws IOException
    {
        JsonNode node = parser.getCodec().readTree(parser);

        String filerName = valueOrDefault(node, "filerName");
        String transactionText = valueOrDefault(node, "transactionText");
        String moneyText = valueOrDefault(node, "moneyText");
        String ownership = valueOrDefault(node, "ownership");
        Transaction.LocalDateWrapper startDate = new Transaction.LocalDateWrapper(
                LocalDate.parse(valueOrDefault(node.get("startDate"), "fmt")));
        String value = valueOrDefault(node.get("value"), "raw");
        String filerRelation = valueOrDefault(node, "filerRelation");
        String shares = valueOrDefault(node.get("shares"), "raw");
        String filerUrl = valueOrDefault(node, "filerUrl");
        long maxAge = Long.parseLong(valueOrDefault(node, "maxAge"));

        return new Transaction(filerName, transactionText, moneyText, ownership,
                startDate, value, filerRelation, shares, filerUrl, maxAge);
    }

    private String valueOrDefault(JsonNode node, String field) {
        if (node == null) return "";

        JsonNode fieldNode = node.get(field);
        return fieldNode != null ? fieldNode.asText() : "";
    }
}
