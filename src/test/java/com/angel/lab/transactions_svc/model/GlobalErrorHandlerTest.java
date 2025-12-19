package com.angel.lab.transactions_svc.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import reactor.test.StepVerifier;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@DisplayName("GlobalErrorHandler Tests")
class GlobalErrorHandlerTest {

    private GlobalErrorHandler globalErrorHandler;

    @BeforeEach
    void setUp() {
        globalErrorHandler = new GlobalErrorHandler();
    }

    @Test
    @DisplayName("should handle BusinessException with BAD_REQUEST status")
    void whenBusinessExceptionThrownShouldReturnBadRequest() {
        BusinessException exception = new BusinessException("Insufficient funds");
        
        var result = globalErrorHandler.handleBiz(exception);
        
        StepVerifier.create(result)
            .assertNext(response -> {
                assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
                assertNotNull(response.getBody());
                assertEquals("Insufficient funds", response.getBody().get("error"));
            })
            .verifyComplete();
    }

    @Test
    @DisplayName("should include error message in response body for BusinessException")
    void whenBusinessExceptionThrownBodyShouldContainMessage() {
        String errorMessage = "Transaction rejected by risk service";
        BusinessException exception = new BusinessException(errorMessage);
        
        var result = globalErrorHandler.handleBiz(exception);
        
        StepVerifier.create(result)
            .assertNext(response -> {
                Map<String, Object> body = response.getBody();
                assertEquals(errorMessage, body.get("error"));
            })
            .verifyComplete();
    }

    @Test
    @DisplayName("should handle generic Exception with INTERNAL_SERVER_ERROR status")
    void whenGenericExceptionThrownShouldReturnInternalServerError() {
        Exception exception = new RuntimeException("Unexpected error");
        
        var result = globalErrorHandler.handleGen(exception);
        
        StepVerifier.create(result)
            .assertNext(response -> {
                assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
                assertNotNull(response.getBody());
                assertEquals("internal_error", response.getBody().get("error"));
            })
            .verifyComplete();
    }

    @Test
    @DisplayName("should return generic error message for any Exception")
    void whenGenericExceptionThrownBodyShouldContainGenericMessage() {
        Exception exception = new Exception("Specific database error");
        
        var result = globalErrorHandler.handleGen(exception);
        
        StepVerifier.create(result)
            .assertNext(response -> {
                Map<String, Object> body = response.getBody();
                assertEquals("internal_error", body.get("error"));
            })
            .verifyComplete();
    }
}
