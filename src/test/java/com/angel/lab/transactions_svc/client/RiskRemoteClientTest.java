package com.angel.lab.transactions_svc.client;

import com.angel.lab.transactions_svc.service.RiskService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriBuilder;
import reactor.core.publisher.Mono;
import java.math.BigDecimal;
import java.net.URI;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RiskRemoteClientTest {
    @Mock
    private WebClient webClient;

    @Mock
    WebClient.RequestHeadersUriSpec uriSpec;

    @Mock
    WebClient.RequestHeadersSpec headersSpec;

    @Mock
    private WebClient.ResponseSpec responseSpec;

    @Mock
    private RiskService riskService;

    @InjectMocks
    private RiskRemoteClient riskRemoteClient;


    @BeforeEach
    void setUp() {

        ReflectionTestUtils.setField(riskRemoteClient, "legacy", riskService);
    }

    @Test
    void isAllowed_returnsTrue() {

        when(webClient.get()).thenReturn(uriSpec);

        when(uriSpec.uri(Mockito.<Function<UriBuilder, URI>>any()))
                .thenReturn(headersSpec);

        when(headersSpec.retrieve()).thenReturn(responseSpec);

        when(responseSpec.bodyToMono(Boolean.class))
                .thenReturn(Mono.just(true));

        Boolean result = riskRemoteClient
                .isAllowed("USD", "DEBIT", BigDecimal.valueOf(100))
                .toCompletableFuture()
                .join();

        assertTrue(result);
    }

    @Test
    void isAllowed_returnsFalse() {

        when(webClient.get()).thenReturn(uriSpec);

        when(uriSpec.uri(Mockito.<Function<UriBuilder, URI>>any()))
                .thenReturn(headersSpec);

        when(headersSpec.retrieve()).thenReturn(responseSpec);

        when(responseSpec.bodyToMono(Boolean.class))
                .thenReturn(Mono.just(false));

        Boolean result = riskRemoteClient
                .isAllowed("PEN", "DEBIT", BigDecimal.valueOf(1500))
                .toCompletableFuture()
                .join();

        assertFalse(result);
    }

    @Test
    void whenLegacyServiceExecutesAndReturnsTrue() throws Exception {
        when(riskService.isAllowed("USD", "CREDIT", new BigDecimal("1000")))
            .thenReturn(Mono.just(true));
        
        var result = riskRemoteClient.fallback("USD", "CREDIT", new BigDecimal("1000"));
        
        assertTrue(result.toCompletableFuture().get());
    }

    @Test
    void whenLegacyServiceExecutesAndReturnsFalse() throws Exception {
        when(riskService.isAllowed("EUR", "DEBIT", new BigDecimal("3000")))
            .thenReturn(Mono.just(false));
        
        var result = riskRemoteClient.fallback("EUR", "DEBIT", new BigDecimal("3000"));
        
        assertFalse(result.toCompletableFuture().get());
    }
}
