package org.insider.repository;

import jakarta.persistence.*;
import org.insider.model.Transaction;
import org.insider.repository.entities.SymbolsEntity;
import org.insider.repository.entities.TransactionsEntity;
import org.insider.util.TestUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.Date;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
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
    void saveTransactions_validRecordsWithNewSymbol_saveAllTransactions() {
        int persistSymbolCount = 1;
        int newTransactionCount = 50;
        List<Transaction> transactions = TestUtils.createTransactions(newTransactionCount, "2022-01-01");

        when(entityManagerFactoryMock.createEntityManager()).thenReturn(entityManagerMock);
        when(entityManagerMock.getTransaction()).thenReturn(entityTransactionMock);
        doNothing().when(entityManagerMock).persist(any());
        when(entityManagerMock.createQuery(anyString(), eq(SymbolsEntity.class))).thenReturn(symbolsQueryMock);

        databaseManager.saveTransactions(transactions, "TEST", "GB");

        verify(entityManagerMock, times(newTransactionCount)).persist(any(TransactionsEntity.class));
        verify(entityManagerMock, times(persistSymbolCount)).persist(any(SymbolsEntity.class));
    }

    @Test
    void saveTransactions_validRecordsWithExistingSymbol_saveOnlyNewTransactions() {
        int mergeSymbolCount = 1;
        int newTransactionCount = 10;

        List<Transaction> transactions = TestUtils.createTransactions(40, "2022-01-01");
        transactions.addAll(TestUtils.createTransactions(newTransactionCount, "2023-01-01"));

        when(entityManagerFactoryMock.createEntityManager()).thenReturn(entityManagerMock);
        when(entityManagerMock.getTransaction()).thenReturn(entityTransactionMock);
        doNothing().when(entityManagerMock).persist(any());
        when(entityManagerMock.createQuery(anyString(), eq(SymbolsEntity.class))).thenReturn(symbolsQueryMock);
        when(symbolsQueryMock.getSingleResult()).thenReturn(
                new SymbolsEntity("TEST", "GB", LocalDate.parse("2022-12-31")));

        databaseManager.saveTransactions(transactions, "TEST", "GB");

        verify(entityManagerMock, times(newTransactionCount)).persist(any(TransactionsEntity.class));
        verify(entityManagerMock, times(mergeSymbolCount)).merge(any(SymbolsEntity.class));
    }

    @Test
    void saveTransactions_throwsHibernateException_performsRollback() {
        int newTransactionCount = 1;
        List<Transaction> transactions = TestUtils.createTransactions(newTransactionCount, "2022-01-01");
        String exceptionMessage = "Failure to insert symbol";
        Exception expected = new PersistenceException(exceptionMessage);

        when(entityManagerFactoryMock.createEntityManager()).thenReturn(entityManagerMock);
        when(entityManagerMock.getTransaction()).thenReturn(entityTransactionMock);
        when(entityTransactionMock.isActive()).thenReturn(true);

        // doThrow is used when trying to throw exceptions from void methods
        doThrow(expected).when(entityManagerMock).persist(any(SymbolsEntity.class));
        when(entityManagerMock.createQuery(anyString(), eq(SymbolsEntity.class))).thenReturn(symbolsQueryMock);

        // saveTransactions calls performSaveOperation, while running it, performSaveOperation is
        // called again in the method updateSymbolRecord - where the exception occurs, it is then
        // logged and propagated
        // TODO: Should I just crash the system in the updateSymbolRecord instead?
        Exception actual = assertThrows(PersistenceException.class,
                () -> databaseManager.saveTransactions(transactions, "TEST", "GB"));

        verify(entityTransactionMock, times(2)).rollback();
        assertEquals(expected, actual);
    }

    @Test
    void getTransactionsByRange_validInput_returnsCorrectTransactions() {
        String symbol = "TEST";
        String region = "GB";

        List<TransactionsEntity> expectedDbTransactions =
                TestUtils.createTransactionEntities(2, symbol, region, Date.valueOf("2023-02-02"));
        List<Transaction> expected = TestUtils.convertTransactionEntitiesToTransactions(expectedDbTransactions);

        when(entityManagerFactoryMock.createEntityManager()).thenReturn(entityManagerMock);
        when(entityManagerMock.createQuery(anyString(), eq(TransactionsEntity.class)))
                .thenReturn(transactionsQueryMock);
        when(transactionsQueryMock.getResultList()).thenReturn(expectedDbTransactions);

        List<Transaction> actual = databaseManager
                .getTransactionsByRange(symbol, region, "2023-01-01", "2023-04-30");

        assertEquals(expected, actual);
    }    
}