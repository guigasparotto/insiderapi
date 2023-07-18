package org.insider.repository;

import jakarta.persistence.*;
import org.insider.model.Transaction;
import org.insider.repository.entities.SymbolsEntity;
import org.insider.repository.entities.TransactionsEntity;
import org.insider.util.TestUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.junit.jupiter.params.provider.Arguments;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.Date;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JpaDatabaseManagerTest {
    private final String testSymbol = "TEST";
    private final String testRegion = "GB";

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
        SymbolsEntity expected = new SymbolsEntity(testSymbol, testRegion, LocalDate.now());

        when(symbolsQueryMock.getSingleResult()).thenReturn(expected);
        when(entityManagerFactoryMock.createEntityManager()).thenReturn(entityManagerMock);
        when(entityManagerMock.createQuery(anyString(), eq(SymbolsEntity.class))).thenReturn(symbolsQueryMock);

        Optional<SymbolsEntity> actual = databaseManager.getSymbolRecord(testSymbol, testRegion);

        verify(symbolsQueryMock).setParameter("symbol", testSymbol);
        verify(symbolsQueryMock).setParameter("region", testRegion);
        assertEquals(expected, actual.get());
    }

    @Test
    void getSymbolRecord_recordNotFound_returnsEmptyOptional() {
        Exception expected = new NoResultException("No entity found for query");
        when(symbolsQueryMock.getSingleResult()).thenThrow(expected);
        when(entityManagerFactoryMock.createEntityManager()).thenReturn(entityManagerMock);
        when(entityManagerMock.createQuery(anyString(), eq(SymbolsEntity.class))).thenReturn(symbolsQueryMock);

        Optional<SymbolsEntity> actual = databaseManager.getSymbolRecord(testSymbol, testRegion);
        assertTrue(actual.isEmpty());
    }

    @ParameterizedTest
    @MethodSource("nullParametersProvider")
    void saveTransactions_nullParameter_throwsNullPointerException(
            List<Transaction> transactions, String symbol, String region, String message)
    {
        NullPointerException e = assertThrows(NullPointerException.class, () ->
                databaseManager.saveTransactions(transactions, symbol, region));
        assertEquals(message, e.getMessage());
    }

    @ParameterizedTest
    @MethodSource("emptyParametersProvider")
    void saveTransactions_emptyParameter_throwsIllegalArgumentException(
            List<Transaction> transactions, String symbol, String region, String message)
    {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () ->
                databaseManager.saveTransactions(transactions, symbol, region));
        assertEquals(message, e.getMessage());
    }

    @Test
    void saveTransactions_validRecordsWithNewSymbol_saveAllTransactions() {
        int newTransactionCount = 50;
        List<Transaction> transactions = TestUtils.createTransactions(newTransactionCount, "2022-01-01");

        when(entityManagerFactoryMock.createEntityManager()).thenReturn(entityManagerMock);
        when(entityManagerMock.getTransaction()).thenReturn(entityTransactionMock);
        when(entityManagerMock.createQuery(anyString(), eq(SymbolsEntity.class))).thenReturn(symbolsQueryMock);

        databaseManager.saveTransactions(transactions, testSymbol, testRegion);
        verifyPersistedEntities(newTransactionCount, testSymbol, testRegion, false);
    }

    @Test
    void saveTransactions_validRecordsWithExistingSymbol_saveOnlyNewTransactions() {
        int newTransactionCount = 10;
        List<Transaction> transactions = TestUtils.createTransactions(40, "2022-01-01");
        transactions.addAll(TestUtils.createTransactions(newTransactionCount, "2023-01-01"));

        when(entityManagerFactoryMock.createEntityManager()).thenReturn(entityManagerMock);
        when(entityManagerMock.getTransaction()).thenReturn(entityTransactionMock);
        when(entityManagerMock.createQuery(anyString(), eq(SymbolsEntity.class))).thenReturn(symbolsQueryMock);
        when(symbolsQueryMock.getSingleResult()).thenReturn(
                new SymbolsEntity(testSymbol, testRegion, LocalDate.parse("2022-12-31")));

        databaseManager.saveTransactions(transactions, testSymbol, testRegion);
        verifyPersistedEntities(newTransactionCount, testSymbol, testRegion, true);
    }

    @Test
    void saveTransactions_whenHibernateExceptionIsThrown_performsRollback() {
        int newTransactionCount = 1;
        List<Transaction> transactions = TestUtils.createTransactions(newTransactionCount, "2022-01-01");
        String exceptionMessage = "Failure to insert symbol";
        Exception expected = new PersistenceException(exceptionMessage);

        when(entityManagerFactoryMock.createEntityManager()).thenReturn(entityManagerMock);
        when(entityManagerMock.getTransaction()).thenReturn(entityTransactionMock);
        when(entityTransactionMock.isActive()).thenReturn(true);

        // doThrow is used when trying to throw exceptions from void methods
        // the exception forced to verify if the method under test performs rollback
        doThrow(expected).when(entityManagerMock).persist(any(SymbolsEntity.class));
        when(entityManagerMock.createQuery(anyString(), eq(SymbolsEntity.class))).thenReturn(symbolsQueryMock);

        // saveTransactions calls performSaveOperation, while running it, performSaveOperation is
        // called again in the method updateSymbolRecord - where the exception occurs, it is then
        // logged and propagated
        // TODO: Should I just crash the system in the updateSymbolRecord instead?
        Exception actual = assertThrows(PersistenceException.class,
                () -> databaseManager.saveTransactions(transactions, testSymbol, testRegion));

        verify(entityTransactionMock, times(2)).rollback();
        assertEquals(expected, actual);
    }

    @Test
    void getTransactionsByRange_validInput_returnsCorrectTransactions() {
        List<TransactionsEntity> expectedDbTransactions =
                TestUtils.createTransactionEntities(2, testSymbol, testRegion, Date.valueOf("2023-02-02"));
        List<Transaction> expected = TestUtils.convertTransactionEntitiesToTransactions(expectedDbTransactions);

        when(entityManagerFactoryMock.createEntityManager()).thenReturn(entityManagerMock);
        when(entityManagerMock.createQuery(anyString(), eq(TransactionsEntity.class)))
                .thenReturn(transactionsQueryMock);
        when(transactionsQueryMock.getResultList()).thenReturn(expectedDbTransactions);

        List<Transaction> actual = databaseManager
                .getTransactionsByRange(testSymbol, testRegion, "2023-01-01", "2023-04-30");

        verify(transactionsQueryMock).setParameter("symbol", testSymbol);
        verify(transactionsQueryMock).setParameter("region", testRegion);
        assertEquals(expected, actual);
    }

    private void verifyPersistedEntities(int transactionCount, String symbol, String region, boolean existingSymbol) {
        int expectedSymbolCalls = 1;

        // Create ArgumentCaptor for SymbolsEntity and TransactionsEntity, this enables
        // checking if the mocked methods received the correct data. ArgumentCaptor queries
        // against Mockito's context, so we can have access to method calls and arguments used
        ArgumentCaptor<SymbolsEntity> symbolsCaptor = ArgumentCaptor.forClass(SymbolsEntity.class);
        ArgumentCaptor<TransactionsEntity> transactionsCaptor = ArgumentCaptor.forClass(TransactionsEntity.class);

        // Verify and capture
        verify(entityManagerMock, times(transactionCount)).persist(transactionsCaptor.capture());

        if(existingSymbol) {
            verify(entityManagerMock, times(expectedSymbolCalls)).merge(symbolsCaptor.capture());
        } else {
            verify(entityManagerMock, times(expectedSymbolCalls)).persist(symbolsCaptor.capture());
        }

        // Validate SymbolsEntity
        SymbolsEntity persistedSymbol = symbolsCaptor.getValue();
        assertEquals(symbol, persistedSymbol.getSymbol());
        assertEquals(region, persistedSymbol.getRegion());

        // Check that the persisted TransactionsEntity instances are correct
        // This is just validating the count, but is possible to access the records and check
        // if the parameters are correct, which doesn't seem to be necessary here
        List<TransactionsEntity> persistedTransactions = transactionsCaptor.getAllValues();
        assertEquals(transactionCount, persistedTransactions.size());
    }

    static Stream<Arguments> nullParametersProvider() {
        String validRegion = "GB";
        String validSymbol = "TEST";
        List<Transaction> transactions = TestUtils.createTransactions(1, "2022-01-01");

        return Stream.of(
                Arguments.of(transactions, null, validRegion, "symbol cannot be null"),
                Arguments.of(transactions, validSymbol, null, "region cannot be null"),
                Arguments.of(null, validSymbol, validRegion, "transactions cannot be null")
        );
    }
    static Stream<Arguments> emptyParametersProvider() {
        String validRegion = "GB";
        String validSymbol = "TEST";
        List<Transaction> transactions = TestUtils.createTransactions(1, "2022-01-01");
        List<Transaction> emptyList = new ArrayList<>();

        return Stream.of(
                Arguments.of(transactions, "", validRegion, "All fields must be non-empty"),
                Arguments.of(transactions, validSymbol, "", "All fields must be non-empty"),
                Arguments.of(emptyList, validSymbol, validRegion, "All fields must be non-empty")
        );
    }
}