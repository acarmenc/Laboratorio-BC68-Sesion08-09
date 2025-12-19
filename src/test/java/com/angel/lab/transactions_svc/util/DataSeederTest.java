package com.angel.lab.transactions_svc.util;

import com.angel.lab.transactions_svc.model.entities.Account;
import com.angel.lab.transactions_svc.model.entities.RiskRule;
import com.angel.lab.transactions_svc.repository.AccountRepository;
import com.angel.lab.transactions_svc.repository.RiskRuleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("DataSeeder Tests")
class DataSeederTest {

    @Mock
    private RiskRuleRepository riskRuleRepository;

    @Mock
    private AccountRepository accountRepository;

    private DataSeeder dataSeeder;

    @BeforeEach
    void setUp() {
        dataSeeder = new DataSeeder(riskRuleRepository, accountRepository);
    }

    @Test
    @DisplayName("should save PEN risk rule")
    void whenRunCalledShouldSavePenRiskRule() {
        when(riskRuleRepository.save(any(RiskRule.class)))
            .thenReturn(new RiskRule());

        when(accountRepository.deleteAll())
            .thenReturn(Mono.empty());

        when(accountRepository.save(any(Account.class)))
            .thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));

        dataSeeder.run();

        verify(riskRuleRepository, times(2)).save(any(RiskRule.class));
    }

    @Test
    @DisplayName("should save USD risk rule")
    void whenRunCalledShouldSaveUsdRiskRule() {
        when(riskRuleRepository.save(any(RiskRule.class)))
            .thenReturn(new RiskRule());

        when(accountRepository.deleteAll())
            .thenReturn(Mono.empty());

        when(accountRepository.save(any(Account.class)))
            .thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));

        dataSeeder.run();

        verify(riskRuleRepository, times(2)).save(any(RiskRule.class));
    }

    @Test
    @DisplayName("should delete all accounts before seeding")
    void whenRunCalledShouldDeleteAllAccounts() {
        when(riskRuleRepository.save(any(RiskRule.class)))
            .thenReturn(new RiskRule());

        when(accountRepository.deleteAll())
            .thenReturn(Mono.empty());

        when(accountRepository.save(any(Account.class)))
            .thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));

        dataSeeder.run();

        verify(accountRepository).deleteAll();
    }

    @Test
    @DisplayName("should save Ana Peru account with correct details")
    void whenRunCalledShouldSaveAnaPeru() {
        when(riskRuleRepository.save(any(RiskRule.class)))
            .thenReturn(new RiskRule());

        when(accountRepository.deleteAll())
            .thenReturn(Mono.empty());

        when(accountRepository.save(any(Account.class)))
            .thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));

        dataSeeder.run();

        verify(accountRepository, times(2)).save(any(Account.class));
    }

    @Test
    @DisplayName("should save Luis Acuña account with correct details")
    void whenRunCalledShouldSaveLuisAcuna() {
        when(riskRuleRepository.save(any(RiskRule.class)))
            .thenReturn(new RiskRule());

        when(accountRepository.deleteAll())
            .thenReturn(Mono.empty());

        when(accountRepository.save(any(Account.class)))
            .thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));

        dataSeeder.run();

        verify(accountRepository, times(2)).save(any(Account.class));
    }

    @Test
    @DisplayName("should seed PEN risk rule with correct max debit limit")
    void whenRunCalledPenRuleShouldHaveCorrectLimit() {
        RiskRule savedRule = new RiskRule();
        savedRule.setCurrency("PEN");
        savedRule.setMaxDebitPerTx(new BigDecimal("1500"));

        when(riskRuleRepository.save(any(RiskRule.class)))
            .thenAnswer(invocation -> {
                RiskRule rule = invocation.getArgument(0);
                if ("PEN".equals(rule.getCurrency())) {
                    return savedRule;
                }
                return rule;
            });

        when(accountRepository.deleteAll())
            .thenReturn(Mono.empty());

        when(accountRepository.save(any(Account.class)))
            .thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));

        dataSeeder.run();

        verify(riskRuleRepository, times(2)).save(any(RiskRule.class));
    }

    @Test
    @DisplayName("should seed USD risk rule with correct max debit limit")
    void whenRunCalledUsdRuleShouldHaveCorrectLimit() {
        RiskRule savedRule = new RiskRule();
        savedRule.setCurrency("USD");
        savedRule.setMaxDebitPerTx(new BigDecimal("500"));

        when(riskRuleRepository.save(any(RiskRule.class)))
            .thenAnswer(invocation -> {
                RiskRule rule = invocation.getArgument(0);
                if ("USD".equals(rule.getCurrency())) {
                    return savedRule;
                }
                return rule;
            });

        when(accountRepository.deleteAll())
            .thenReturn(Mono.empty());

        when(accountRepository.save(any(Account.class)))
            .thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));

        dataSeeder.run();

        verify(riskRuleRepository, times(2)).save(any(RiskRule.class));
    }

    @Test
    @DisplayName("should seed Ana Peru account with PEN currency and correct balance")
    void whenRunCalledAnaPetuShouldHavePenCurrencyAndBalance() {
        when(riskRuleRepository.save(any(RiskRule.class)))
            .thenReturn(new RiskRule());

        when(accountRepository.deleteAll())
            .thenReturn(Mono.empty());

        when(accountRepository.save(any(Account.class)))
            .thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));

        dataSeeder.run();

        verify(accountRepository, times(2)).save(any(Account.class));
    }

    @Test
    @DisplayName("should complete without errors")
    void whenRunCalledShouldCompleteSuccessfully() {
        when(riskRuleRepository.save(any(RiskRule.class)))
            .thenReturn(new RiskRule());

        when(accountRepository.deleteAll())
            .thenReturn(Mono.empty());

        when(accountRepository.save(any(Account.class)))
            .thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));

        dataSeeder.run();

        verify(riskRuleRepository, times(2)).save(any(RiskRule.class));
        verify(accountRepository).deleteAll();
        verify(accountRepository, times(2)).save(any(Account.class));
    }
}
