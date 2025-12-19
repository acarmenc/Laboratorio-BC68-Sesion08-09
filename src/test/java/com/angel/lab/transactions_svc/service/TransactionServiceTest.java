package com.angel.lab.transactions_svc.service;

import com.angel.lab.transactions_svc.client.RiskRemoteClient;
import com.angel.lab.transactions_svc.model.CreateTxRequest;
import com.angel.lab.transactions_svc.model.entities.Account;
import com.angel.lab.transactions_svc.model.entities.Transaction;
import com.angel.lab.transactions_svc.repository.AccountRepository;
import com.angel.lab.transactions_svc.repository.RiskRuleRepository;
import com.angel.lab.transactions_svc.repository.TransactionRepository;
import com.angel.lab.transactions_svc.util.LogContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import reactor.core.publisher.Sinks;
import reactor.test.StepVerifier;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
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
    private CreateTxRequest request;

    @BeforeEach
    public void setUp(){
        account = Account.builder()
                .id("acc123")
                .number("001-0001")
                .holderName("Ana Peru")
                .currency("PEN")
                .balance(new BigDecimal(1900))
                .build();

        request = new CreateTxRequest();
        request.setAccountNumber("001-0001");
        request.setType("DEBIT");
        request.setAmount(new BigDecimal(600));
    }

    @Test
    void debitOk() {
        var req = new CreateTxRequest();
        req.setAccountNumber("001-0001"); req.setType("DEBIT"); req.setAmount(new
                BigDecimal("100"));
        StepVerifier.create(service.create(req))
                .assertNext(tx -> assertEquals("OK", tx.getStatus()))
                .verifyComplete();
    }

}
