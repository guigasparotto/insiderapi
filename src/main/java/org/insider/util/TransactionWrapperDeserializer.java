package org.insider.util;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import org.insider.model.Transaction;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class TransactionWrapperDeserializer extends StdDeserializer<TransactionWrapper> {

    public TransactionWrapperDeserializer() {
        super(TransactionWrapper.class);
    }

    @Override
    public TransactionWrapper deserialize(
            JsonParser parser,
            DeserializationContext context) throws IOException
    {
        JsonNode node = parser.getCodec().readTree(parser);
        TransactionDeserializer transactionDeserializer = new TransactionDeserializer();
        List<Transaction> transactions = new ArrayList<>();

        for (JsonNode transactionNode : node) {
            Transaction transaction =
                    transactionDeserializer.deserialize(
                            transactionNode.traverse(parser.getCodec()), context);
            transactions.add(transaction);
        }

        return new TransactionWrapper(transactions);
    }
}