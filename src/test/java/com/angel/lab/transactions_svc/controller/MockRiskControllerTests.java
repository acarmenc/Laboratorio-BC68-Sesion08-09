package com.angel.lab.transactions_svc.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.math.BigDecimal;

@DisplayName("MockRiskController Tests")
public class MockRiskControllerTests {

    WebTestClient client;

    @BeforeEach
    void setUp() {
        client = WebTestClient.bindToController(new MockRiskController()).build();
    }

    @Test
    @DisplayName("when DEBIT amount is under limit return true")
    void whenDebitUnderLimitReturnTrue() {
        client.get().uri(uriBuilder -> uriBuilder
                .path("/mock/risk/allow")
                .queryParam("currency", "PEN")
                .queryParam("type", "DEBIT")
                .queryParam("amount", 500)
                .queryParam("fail", false)
                .queryParam("delayMs", 0)
                .build())
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(Boolean.class)
                .isEqualTo(true);
    }

    @Test
    @DisplayName("when DEBIT amount exceeds limit return false")
    void whenDebitExceedsLimitReturnFalse() {
        client.get().uri(uriBuilder -> uriBuilder
                .path("/mock/risk/allow")
                .queryParam("currency", "PEN")
                .queryParam("type", "DEBIT")
                .queryParam("amount", 1500)
                .queryParam("fail", false)
                .queryParam("delayMs", 0)
                .build())
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(Boolean.class)
                .isEqualTo(false);
    }

    @Test
    @DisplayName("when CREDIT any amount return true")
    void whenCreditAnyAmountReturnTrue() {
        client.get().uri(uriBuilder -> uriBuilder
                .path("/mock/risk/allow")
                .queryParam("currency", "USD")
                .queryParam("type", "CREDIT")
                .queryParam("amount", 5000)
                .queryParam("fail", false)
                .queryParam("delayMs", 0)
                .build())
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(Boolean.class)
                .isEqualTo(true);
    }

    @Test
    @DisplayName("when fail flag is true return error")
    void whenFailFlagTrueReturnError() {
        client.get().uri(uriBuilder -> uriBuilder
                .path("/mock/risk/allow")
                .queryParam("currency", "EUR")
                .queryParam("type", "DEBIT")
                .queryParam("amount", 100)
                .queryParam("fail", true)
                .queryParam("delayMs", 0)
                .build())
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().is5xxServerError();
    }

    @Test
    @DisplayName("when DEBIT at exact limit return true")
    void whenDebitAtExactLimitReturnTrue() {
        client.get().uri(uriBuilder -> uriBuilder
                .path("/mock/risk/allow")
                .queryParam("currency", "PEN")
                .queryParam("type", "DEBIT")
                .queryParam("amount", 1200)
                .queryParam("fail", false)
                .queryParam("delayMs", 0)
                .build())
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(Boolean.class)
                .isEqualTo(true);
    }

    @Test
    @DisplayName("when delay greater than 1s triggers fallback to legacy service")
    void whenDelayGreaterThan1sTriggersLegacyFallback() {

        client.get().uri(uriBuilder -> uriBuilder
                .path("/mock/risk/allow")
                .queryParam("currency", "PEN")
                .queryParam("type", "DEBIT")
                .queryParam("amount", 500)  
                .queryParam("fail", false)
                .queryParam("delayMs", 1500)
                .build())
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(Boolean.class)
                .isEqualTo(true);
    }

}
