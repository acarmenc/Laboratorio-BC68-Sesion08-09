package com.angel.lab.transactions_svc.service;

import com.angel.lab.transactions_svc.model.BusinessException;
import com.angel.lab.transactions_svc.model.CreateTxRequest;
import com.angel.lab.transactions_svc.model.entities.Account;
import com.angel.lab.transactions_svc.model.entities.Transaction;
import com.angel.lab.transactions_svc.repository.AccountRepository;
import com.angel.lab.transactions_svc.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;
import reactor.core.scheduler.Schedulers;

import java.math.BigDecimal;
import java.time.Instant;

@Service
@RequiredArgsConstructor
public class TransactionService {
    private final AccountRepository accountRepo;
    private final TransactionRepository txRepo;
    private final RiskService riskService;
    private final Sinks.Many<Transaction> txSink;
    public Mono<Transaction> create(CreateTxRequest req) {
        return accountRepo.findByNumber(req.getAccountNumber())
                .switchIfEmpty(Mono.error(new BusinessException("account_not_found")))
                .flatMap(acc -> validateAndApply(acc, req))
                .onErrorMap(IllegalStateException.class, e -> new
                        BusinessException(e.getMessage()));
    }
    private Mono<Transaction> validateAndApply(Account acc, CreateTxRequest req)
    {
        String type = req.getType().toUpperCase();
        BigDecimal amount = req.getAmount();
// 1) Riesgo (bloqueante envuelto -> elastic)
        return riskService.isAllowed(acc.getCurrency(), type, amount)
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
    }
    public Flux<Transaction> byAccount(String accountNumber) {
        return accountRepo.findByNumber(accountNumber)
                .switchIfEmpty(Mono.error(new BusinessException("account_not_found")))
                .flatMapMany(acc ->
                        txRepo.findByAccountIdOrderByTimestampDesc(acc.getId()));
    }
    public Flux<ServerSentEvent<Transaction>> stream() {
        return txSink.asFlux()
                .map(tx -> ServerSentEvent.builder(tx).event("transaction").build());
    }
}