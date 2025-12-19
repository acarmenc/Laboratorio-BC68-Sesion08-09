package com.angel.lab.transactions_svc.service;

import com.angel.lab.transactions_svc.model.entities.RiskRule;
import com.angel.lab.transactions_svc.repository.RiskRuleRepository;
import com.angel.lab.transactions_svc.util.LogContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RiskServiceTest {
    
    @Mock
    private RiskRuleRepository riskRuleRepository;
    
    @Mock
    private LogContext logContext;
    
    @InjectMocks
    private RiskService riskService;

    private RiskRule penRule;

    @BeforeEach
    public void setUp(){
        penRule = RiskRule.builder()
                .currency("PEN")
                .maxDebitPerTx(new BigDecimal("1000"))
                .build();
        

        lenient().when(logContext.withMdc(any())).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void whenDebitAllowedUnderLimit() {
        
        when(riskRuleRepository.findFirstByCurrency("PEN"))
                .thenReturn(Optional.of(penRule));
        
        StepVerifier.create(riskService.isAllowed("PEN", "DEBIT", new BigDecimal("500")))
                .expectNext(true)
                .verifyComplete();
    }

    @Test
    void whenDebitRejectedExceedsLimit() {
        
        when(riskRuleRepository.findFirstByCurrency("PEN"))
                .thenReturn(Optional.of(penRule));
        
        StepVerifier.create(riskService.isAllowed("PEN", "DEBIT", new BigDecimal("1100")))
                .expectNext(false)
                .verifyComplete();
    }

    @Test
    void whenOtherTypeAlwaysAllowed() {
        
        when(riskRuleRepository.findFirstByCurrency("PEN"))
                .thenReturn(Optional.of(penRule));
        
        StepVerifier.create(riskService.isAllowed("PEN", "CREDIT", new BigDecimal("5000")))
                .expectNext(true)
                .verifyComplete();
    }

    @Test
    void whenCurrencyNotFoundDebitRejected() {
        when(riskRuleRepository.findFirstByCurrency("USD"))
                .thenReturn(Optional.empty());
        
        StepVerifier.create(riskService.isAllowed("USD", "DEBIT", new BigDecimal("100")))
                .expectNext(false)
                .verifyComplete();
    }
}