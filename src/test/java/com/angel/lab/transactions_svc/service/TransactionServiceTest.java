package com.angel.lab.transactions_svc.service;

import com.angel.lab.transactions_svc.client.RiskRemoteClient;
import com.angel.lab.transactions_svc.model.BusinessException;
import com.angel.lab.transactions_svc.model.CreateTxRequest;
import com.angel.lab.transactions_svc.model.entities.Account;
import com.angel.lab.transactions_svc.model.entities.Transaction;
import com.angel.lab.transactions_svc.repository.AccountRepository;
import com.angel.lab.transactions_svc.repository.TransactionRepository;
import com.angel.lab.transactions_svc.util.LogContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationContext;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private RiskRemoteClient riskRemoteClient;

    @Mock
    private Sinks.Many<Transaction> txSink;

    @Mock
    private LogContext logContext;

    @InjectMocks
    private TransactionService service;

    private Account account;

    @BeforeEach
    public void setUp(){
        account = Account.builder()
                .id("acc123")
                .number("001-0001")
                .holderName("Ana Peru")
                .currency("PEN")
                .balance(new BigDecimal("5000"))
                .build();

        lenient().when(logContext.withMdc(any())).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void whenGetTransactionsByAccount(){

        Transaction tx1_acc123 = Transaction.builder()
                .id("tx1")
                .accountId("acc123")
                .type("DEBIT")
                .amount(new BigDecimal("100"))
                .status("OK")
                .timestamp(Instant.now())
                .build();

        Transaction tx2_acc123 = Transaction.builder()
                .id("tx2")
                .accountId("acc123")
                .type("CREDIT")
                .amount(new BigDecimal("500"))
                .status("OK")
                .timestamp(Instant.now())
                .build();

        when(accountRepository.findByNumber("001-0001"))
                .thenReturn(Mono.just(account));

        when(transactionRepository.findByAccountIdOrderByTimestampDesc("acc123"))
                .thenReturn(Flux.just(tx1_acc123, tx2_acc123));

        StepVerifier.create(service.byAccount("001-0001"))
                .assertNext(tx -> {
                    assertEquals("acc123", tx.getAccountId());
                })
                .assertNext(tx -> {
                    assertEquals("acc123", tx.getAccountId());
                })
                .expectComplete()
                .verify();
    }

    @Test
    void whenGetTransactionsByAccountNotFound(){
        when(accountRepository.findByNumber("999-9999"))
                .thenReturn(Mono.empty());

        StepVerifier.create(service.byAccount("999-9999"))
                .expectError(BusinessException.class)
                .verify();
    }

    @Test
    void whenCreateTransactionSuccess() throws Exception {
        CreateTxRequest request = CreateTxRequest.builder().accountNumber("001-0001").type( "DEBIT").amount(new BigDecimal("100")).build();

        Account updatedAccount = Account.builder()
                .id("acc123")
                .number("001-0001")
                .holderName("Ana Peru")
                .currency("PEN")
                .balance(new BigDecimal("4900"))
                .build();

        Transaction savedTransaction = Transaction.builder()
                .id("tx_new")
                .accountId("acc123")
                .type("DEBIT")
                .amount(new BigDecimal("100"))
                .status("OK")
                .timestamp(Instant.now())
                .build();

        when(accountRepository.findByNumber("001-0001"))
                .thenReturn(Mono.just(account));
        
        when(riskRemoteClient.isAllowed("PEN", "DEBIT", new BigDecimal("100")))
                .thenReturn(CompletableFuture.completedFuture(true));

        when(accountRepository.save(any(Account.class)))
                .thenReturn(Mono.just(updatedAccount));

        when(transactionRepository.save(any(Transaction.class)))
                .thenReturn(Mono.just(savedTransaction));

        StepVerifier.create(service.create(request))
                .assertNext(tx -> {
                    assertEquals("acc123", tx.getAccountId());
                    assertEquals("DEBIT", tx.getType());
                    assertEquals(new BigDecimal("100"), tx.getAmount());
                })
                .expectComplete()
                .verify();
    }

    @Test
    void whenCreateTransactionInsufficientFundsClient() throws Exception {
        CreateTxRequest request = new CreateTxRequest("001-0002", "DEBIT", new BigDecimal("1100"));

        Account account2 = Account.builder().id("acc111")
                .number("001-0002")
                .holderName("Prueba")
                .currency("PEN")
                .balance(new BigDecimal("1000"))
                .build();

        when(accountRepository.findByNumber("001-0002"))
                .thenReturn(Mono.just(account2));
        
        when(riskRemoteClient.isAllowed("PEN", "DEBIT", new BigDecimal("1100")))
                .thenReturn(CompletableFuture.completedFuture(true));

        StepVerifier.create(service.create(request))
                .expectError(BusinessException.class)
                .verify();
    }

    @Test
    void whenCreateTransactionRiskRejectedClient() throws Exception {
        CreateTxRequest request = new CreateTxRequest("001-0001", "DEBIT", new BigDecimal("2000"));

        when(accountRepository.findByNumber("001-0001"))
                .thenReturn(Mono.just(account));
        
        when(riskRemoteClient.isAllowed("PEN", "DEBIT", new BigDecimal("2000")))
                .thenReturn(CompletableFuture.completedFuture(false));

        StepVerifier.create(service.create(request))
                .expectError(BusinessException.class)
                .verify();
    }
}
