package com.angel.lab.transactions_svc.service;

import com.angel.lab.transactions_svc.model.entities.RiskRule;
import com.angel.lab.transactions_svc.repository.RiskRuleRepository;
import com.angel.lab.transactions_svc.util.LogContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.math.BigDecimal;

@Slf4j
@Service
@RequiredArgsConstructor
public class RiskService {
    private final RiskRuleRepository riskRepo;
    private final LogContext logContext;
    public Mono<Boolean> isAllowed(String currency, String type, BigDecimal amount) {
        log.debug("Risk validation currency={} type={} amount={}", currency, type, amount);
        
        Mono<Boolean> serviceLogic = Mono.fromCallable(() ->
                        riskRepo.findFirstByCurrency(currency)
                                .map(RiskRule::getMaxDebitPerTx)
                                .orElse(new BigDecimal("0")))
                .subscribeOn(Schedulers.boundedElastic())
                .map(max -> {
                    boolean allowed = true;
                    if ("DEBIT".equalsIgnoreCase(type)) {
                        allowed = amount.compareTo(max) <= 0;
                    }
                    log.info("Risk result currency={} type={} amount={} allowed={}", currency, type, amount, allowed);
                    return allowed;
                });
        
        return logContext.withMdc(serviceLogic);
    }
}