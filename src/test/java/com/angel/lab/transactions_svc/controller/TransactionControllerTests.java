package com.angel.lab.transactions_svc.controller;

import com.angel.lab.transactions_svc.model.CreateTxRequest;
import com.angel.lab.transactions_svc.model.entities.Transaction;
import com.angel.lab.transactions_svc.service.TransactionService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;

@ExtendWith(MockitoExtension.class)
public class TransactionControllerTests {

    @Mock
    private TransactionService transactionService;

    WebTestClient client;

    @BeforeEach
    void setUp() {
        client = WebTestClient.bindToController(new TransactionController(transactionService)).build();
    }

    @Test
    @DisplayName("when create transaction return created")
    void whenCreateTransactionReturnCreated() {
        CreateTxRequest request = new CreateTxRequest("001-0001", "DEBIT", new BigDecimal("100"));
        
        Transaction transaction = Transaction.builder()
                .id("tx1")
                .accountId("acc123")
                .type("DEBIT")
                .amount(new BigDecimal("100"))
                .status("OK")
                .timestamp(Instant.now())
                .build();

        Mockito.when(transactionService.create(any(CreateTxRequest.class)))
            .thenReturn(Mono.just(transaction));

        client.post().uri("/api/transactions")
            .accept(MediaType.APPLICATION_JSON)
            .contentType(MediaType.APPLICATION_JSON)
            .body(Mono.just(request), CreateTxRequest.class)
            .exchange()
            .expectStatus()
                .isCreated()
                .expectBody(Transaction.class)
                .consumeWith(response -> {
                    Transaction tx = response.getResponseBody();
                    assertEquals(tx.getId(), "tx1");
                    assertEquals(tx.getType(), "DEBIT");
                });
    }

    @Test
    @DisplayName("when get transactions by account return ok")
    void whenGetTransactionsByAccountReturnOk() {
        Transaction tx1 = Transaction.builder()
                .id("tx1")
                .accountId("acc123")
                .type("DEBIT")
                .amount(new BigDecimal("100"))
                .status("OK")
                .timestamp(Instant.now())
                .build();

        Transaction tx2 = Transaction.builder()
                .id("tx2")
                .accountId("acc123")
                .type("CREDIT")
                .amount(new BigDecimal("500"))
                .status("OK")
                .timestamp(Instant.now())
                .build();

        Mockito.when(transactionService.byAccount("001-0001"))
                .thenReturn(Flux.just(tx1, tx2));

        client.get().uri("/api/transactions?accountNumber=001-0001")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBodyList(Transaction.class)
                .consumeWith(response -> {
                    List<Transaction> transactions = response.getResponseBody();
                    Assertions.assertTrue(transactions.size() == 2);
                    assertEquals(transactions.get(0).getType(), "DEBIT");
                    assertEquals(transactions.get(1).getType(), "CREDIT");
                });
    }

    @Test
    @DisplayName("when stream transactions return ok")
    void whenStreamTransactionsReturnOk() {
        Transaction tx = Transaction.builder()
                .id("tx1")
                .accountId("acc123")
                .type("DEBIT")
                .amount(new BigDecimal("100"))
                .status("OK")
                .timestamp(Instant.now())
                .build();

        ServerSentEvent<Transaction> event = ServerSentEvent.builder(tx)
                .event("transaction")
                .build();

        Mockito.when(transactionService.stream())
                .thenReturn(Flux.just(event));

        client.get().uri("/api/stream/transactions")
                .accept(MediaType.TEXT_EVENT_STREAM)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBodyList(ServerSentEvent.class)
                .consumeWith(response -> {
                    List<ServerSentEvent> events = response.getResponseBody();
                    Assertions.assertTrue(events.size() > 0);
                });
    }
}
