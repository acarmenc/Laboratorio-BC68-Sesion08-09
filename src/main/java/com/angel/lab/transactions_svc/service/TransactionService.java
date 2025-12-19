package com.angel.lab.transactions_svc.service;

import com.angel.lab.transactions_svc.client.RiskRemoteClient;
import com.angel.lab.transactions_svc.model.BusinessException;
import com.angel.lab.transactions_svc.model.CreateTxRequest;
import com.angel.lab.transactions_svc.model.entities.Account;
import com.angel.lab.transactions_svc.model.entities.Transaction;
import com.angel.lab.transactions_svc.repository.AccountRepository;
import com.angel.lab.transactions_svc.repository.TransactionRepository;
import com.angel.lab.transactions_svc.util.LogContext;
import lombok.RequiredArgsConstructor;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.stereotype.Service;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;
import reactor.core.scheduler.Schedulers;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.time.Instant;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionService {
    private final AccountRepository accountRepo;
    private final TransactionRepository txRepo;
    //private final RiskService riskService;
    private final RiskRemoteClient riskRemoteClient;
    private final Sinks.Many<Transaction> txSink;
    private final LogContext logContext;

    public Mono<Transaction> create(CreateTxRequest req) {

        log.debug("Creating tx {}", req);

        Mono<Transaction> serviceLogicMono = accountRepo.findByNumber(req.getAccountNumber())
                .switchIfEmpty(Mono.error(new BusinessException("account_not_found")))
                .flatMap(acc -> validateAndApply(acc, req))
                .onErrorMap(IllegalStateException.class, e -> new BusinessException(e.getMessage()));

        return logContext.withMdc(serviceLogicMono)
                .doOnSuccess(tx ->
                        log.info("tx_created account={} amount={}",
                                req.getAccountNumber(), req.getAmount()
                        )
                );
    }
    private Mono<Transaction> validateAndApply(Account acc, CreateTxRequest req)
    {
        String type = req.getType().toUpperCase();
        BigDecimal amount = req.getAmount();

        Mono<Transaction> serviceLogic = 
                // 1) Verificar riesgo con cliente remoto - Circuit breaker + retry + timeLimiter + fallback

                        Mono.fromCompletionStage(riskRemoteClient.isAllowed(acc.getCurrency(), type, amount))
                                .flatMap(allowed -> {
                                if (!allowed) return Mono.error(new BusinessException("risk_rejected"));
                // 2) Reglas de negocio
                                if ("DEBIT".equals(type) && acc.getBalance().compareTo(amount) < 0) {
                                        return Mono.error(new BusinessException("insuCicient_funds"));
                                }
                // 3) Actualiza balance (CPU-light, podemos publishOn paralelo si deseamos)
                                return Mono.just(acc).publishOn(Schedulers.parallel())
                                        .map(a -> {
                                                BigDecimal newBal = "DEBIT".equals(type) ?
                                                        a.getBalance().subtract(amount) : a.getBalance().add(amount);
                                                a.setBalance(newBal);
                                                return a;
                                        })
                                        .flatMap(accountRepo::save)
                // 4) Persiste transacción
                                        .flatMap(saved -> txRepo.save(Transaction.builder()
                                                .accountId(saved.getId())
                                                .type(type)
                                                .amount(amount)
                                                .timestamp(Instant.now())
                                                .status("OK")
                                                .build()))
                // 5) Notifica por SSE
                                        .doOnNext(tx -> txSink.tryEmitNext(tx));
                                });
        return logContext.withMdc(serviceLogic)
                .doOnSuccess(tx ->
                        log.info("tx_created account={} amount={}",
                                req.getAccountNumber(), req.getAmount()
                        )
                );
    }
    public Flux<Transaction> byAccount(String accountNumber) {
        log.debug("Searching transactions for account={}", accountNumber);
        
        Flux<Transaction> serviceLogic = accountRepo.findByNumber(accountNumber)
                .switchIfEmpty(Mono.error(new BusinessException("account_not_found")))
                .flatMapMany(acc -> {
                    log.info("Account found, loading transactions account={}", accountNumber);
                    return txRepo.findByAccountIdOrderByTimestampDesc(acc.getId());
                });
        
        return serviceLogic;
    }
    public Flux<ServerSentEvent<Transaction>> stream() {
        log.debug("Opening transaction stream");
        
        return txSink.asFlux()
                .map(tx -> {
                    log.info("Streaming transaction type={} amount={}", tx.getType(), tx.getAmount());
                    return ServerSentEvent.builder(tx).event("transaction").build();
                });
    }
}