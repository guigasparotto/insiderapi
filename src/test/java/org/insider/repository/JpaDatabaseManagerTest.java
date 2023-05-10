package org.insider.repository;

import jakarta.persistence.*;
import org.insider.model.Transaction;
import org.insider.repository.entities.SymbolsEntity;
import org.insider.repository.entities.TransactionsEntity;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.Date;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JpaDatabaseManagerTest {

    @Mock
    private EntityManagerFactory entityManagerFactoryMock;

    @Mock
    private EntityManager entityManagerMock;

    @Mock
    private EntityTransaction entityTransactionMock;

    @Mock
    private TypedQuery<SymbolsEntity> symbolsQueryMock;

    @Mock
    private TypedQuery<TransactionsEntity> transactionsQueryMock;

    @InjectMocks
    private JpaDatabaseManager databaseManager;

    @Test
    void getSymbolRecord_recordFound_returnsCorrectEntity() {
        String symbol = "TEST";
        String region = "GB";
        SymbolsEntity expected = new SymbolsEntity(symbol, region, LocalDate.now());

        when(symbolsQueryMock.getSingleResult()).thenReturn(expected);
        when(entityManagerFactoryMock.createEntityManager()).thenReturn(entityManagerMock);
        when(entityManagerMock.createQuery(anyString(), eq(SymbolsEntity.class))).thenReturn(symbolsQueryMock);

        SymbolsEntity actual = databaseManager.getSymbolRecord(symbol, region);

        verify(symbolsQueryMock).setParameter("symbol", symbol);
        verify(symbolsQueryMock).setParameter("region", region);
        assertEquals(expected, actual);
    }

    @Test
    void getSymbolRecord_recordNotFound_throwsNoResultException() {
        Exception expected = new NoResultException("No entity found for query");
        when(symbolsQueryMock.getSingleResult()).thenThrow(expected);
        when(entityManagerFactoryMock.createEntityManager()).thenReturn(entityManagerMock);
        when(entityManagerMock.createQuery(anyString(), eq(SymbolsEntity.class))).thenReturn(symbolsQueryMock);

        Exception actual = assertThrows(NoResultException.class, () ->
                databaseManager.getSymbolRecord("TEST", "GB"));

        assertEquals("No entity found for query", actual.getMessage());
    }

    @Test
    void saveTransactions_validRecords_throwsNoExceptions() {
        int transactionCount = 50;
        int persistSymbolCount = 1;
        List<Transaction> transactions = createTransactions(transactionCount);

        when(entityManagerFactoryMock.createEntityManager()).thenReturn(entityManagerMock);
        when(entityManagerMock.getTransaction()).thenReturn(entityTransactionMock);
        doNothing().when(entityManagerMock).persist(any());
        when(entityManagerMock.createQuery(anyString(), eq(SymbolsEntity.class))).thenReturn(symbolsQueryMock);

        databaseManager.saveTransactions(transactions, "TEST", "GB");

        verify(entityManagerMock, times(
                transactionCount + persistSymbolCount)).persist(any());
    }

    @Test
    void getTransactionsByRange_validInput_returnsCorrectTransactions() {
        String symbol = "TEST";
        String region = "GB";

        List<TransactionsEntity> expectedDbTransactions =
                createTransactionEntities(2, symbol, region, Date.valueOf("2023-02-02"));

        List<Transaction> expected = convertTransactionsEntityToTransaction(expectedDbTransactions);

        // Creates a list of transactions with older date and add the expected transactions
        List<TransactionsEntity> dbTransactions
                = createTransactionEntities(10, symbol, region, Date.valueOf("2022-10-10"));
        dbTransactions.addAll(expectedDbTransactions);

        when(entityManagerFactoryMock.createEntityManager()).thenReturn(entityManagerMock);
        when(entityManagerMock.getTransaction()).thenReturn(entityTransactionMock);
        when(entityManagerMock.createQuery(anyString(), eq(TransactionsEntity.class)))
                .thenReturn(transactionsQueryMock);
        when(transactionsQueryMock.getResultList()).thenReturn(dbTransactions);

        List<Transaction> actual = databaseManager
                .getTransactionsByRange(symbol, region, "2023-01-01", "2023-04-30");

        assertEquals(expected, actual);
    }

    private List<Transaction> createTransactions(int quantity) {
        List<Transaction> transactions = new ArrayList<>();

        for (var i = 1; i <= quantity; i++) {
            transactions.add(new Transaction(
                    "Guilherme D",
                    "Bought at price " + (double) i + " per share.",
                    "Money text",
                    "D",
                    new Transaction.LocalDateWrapper(LocalDate.of(2022, 1, 1)),
                    String.valueOf(i * 1000),
                    "",
                    "1000",
                    "www.ggd.com",
                    1,
                    "buy",
                    (double) i)
            );
        }

        return transactions;
    }

    private List<TransactionsEntity> createTransactionEntities(
            int quantity, String symbol, String region, Date date)
    {
        List<TransactionsEntity> transactions = new ArrayList<>();

        for (var i = 1; i <= quantity; i++) {
            transactions.add(new TransactionsEntity(
                    symbol, region,
                    "Guilherme D",
                    "Bought at price " + (double) i + " per share.",
                    "Money text", "D", date,
                    (double) (i * 1000), "", 1000,
                    "www.ggd.com", 1, "buy", (double) i
            ));
        }

        return transactions;
    }

    private List<Transaction> convertTransactionsEntityToTransaction(List<TransactionsEntity> transactionEntities) {
        List<Transaction> transactions = new ArrayList<>();

        for (var entity : transactionEntities) {
            transactions.add(new Transaction(
                    entity.getFilerName(),
                    entity.getTransactionText(),
                    entity.getMoneyText(),
                    entity.getOwnership(),
                    new Transaction.LocalDateWrapper(entity.getStartDate().toLocalDate()),
                    String.valueOf(entity.getValue()),
                    entity.getFilerRelation(),
                    String.valueOf(entity.getShares()),
                    entity.getFilerUrl(),
                    entity.getMaxAge(),
                    entity.getSide(),
                    entity.getPrice()
            ));
        }

        return transactions;
    }
}