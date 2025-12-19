package com.angel.lab.transactions_svc.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;


@DisplayName("LogContext Tests")
class LogContextTest {

    private LogContext logContext;

    @BeforeEach
    void setUp() {
        logContext = new LogContext();
    }

    @Test
    @DisplayName("should wrap mono with MDC context")
    void whenWithMdcCalledShouldWrapMono() {
        Mono<String> originalMono = Mono.just("test-value");
        
        Mono<String> wrappedMono = logContext.withMdc(originalMono);
        
        StepVerifier.create(wrappedMono)
            .expectNext("test-value")
            .verifyComplete();
    }

    @Test
    @DisplayName("should put correlation id in thread context")
    void whenWithMdcCalledShouldSetCorrelationId() {
        Mono<String> originalMono = Mono.just("test-data");
        
        Mono<String> wrappedMono = logContext.withMdc(originalMono)
            .contextWrite(ctx -> ctx.put("corrId", "correlation-123"));
        
        StepVerifier.create(wrappedMono)
            .expectNext("test-data")
            .verifyComplete();
    }

    @Test
    @DisplayName("should handle mono with default correlation id")
    void whenContextMissingCorrelationIdShouldUseDefault() {
        Mono<String> originalMono = Mono.just("value");
        
        Mono<String> wrappedMono = logContext.withMdc(originalMono);
        
        StepVerifier.create(wrappedMono)
            .expectNext("value")
            .verifyComplete();
    }

    @Test
    @DisplayName("should return type parameter correctly")
    void whenWithMdcCalledShouldPreserveType() {
        Mono<Integer> originalMono = Mono.just(42);
        
        Mono<Integer> wrappedMono = logContext.withMdc(originalMono);
        
        StepVerifier.create(wrappedMono)
            .expectNext(42)
            .verifyComplete();
    }

    @Test
    @DisplayName("should handle mono error")
    void whenMonoEmitsErrorShouldPropagateError() {
        Mono<String> originalMono = Mono.error(new RuntimeException("test-error"));
        
        Mono<String> wrappedMono = logContext.withMdc(originalMono);
        
        StepVerifier.create(wrappedMono)
            .expectError(RuntimeException.class)
            .verify();
    }

    @Test
    @DisplayName("should handle empty mono")
    void whenMonoEmptyCompleteWithoutEmit() {
        Mono<String> originalMono = Mono.empty();
        
        Mono<String> wrappedMono = logContext.withMdc(originalMono);
        
        StepVerifier.create(wrappedMono)
            .verifyComplete();
    }

    @Test
    @DisplayName("should propagate context through reactive chain")
    void whenChainedWithOtherMonoShouldPropagateContext() {
        Mono<String> originalMono = Mono.just("first")
            .flatMap(val -> Mono.just(val + "-second"));
        
        Mono<String> wrappedMono = logContext.withMdc(originalMono);
        
        StepVerifier.create(wrappedMono)
            .expectNext("first-second")
            .verifyComplete();
    }
}
