package org.insider.api.serialization;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import org.insider.model.Transaction;

import java.io.IOException;
import java.time.LocalDate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
        Integer maxAge = Integer.valueOf(valueOrDefault(node, "maxAge"));

        String side = getSide(transactionText);
        Double price = getPrice(transactionText);

        return new Transaction(filerName, transactionText, moneyText, ownership,
                startDate, value, filerRelation, shares, filerUrl, maxAge, side, price);
    }

    private String getSide(String transactionText) {
        if (transactionText.startsWith("Bought")) {
            return "buy";
        } else if (transactionText.startsWith("Sold")) {
            return  "sell";
        } else {
            return "not specified";
        }
    }

    private Double getPrice(String transactionText) {
        Pattern pattern = Pattern.compile("(\\d+(\\.\\d+)?)");
        Matcher matcher = pattern.matcher(transactionText);

        if (matcher.find()) {
            return Double.parseDouble(matcher.group(1));
        }

        return null;
    }

    private String valueOrDefault(JsonNode node, String field) {
        if (node == null) {
            if (field.equals("raw")) return "0";

            return "";
        }

        JsonNode fieldNode = node.get(field);
        return fieldNode != null ? fieldNode.asText() : "";
    }
}
