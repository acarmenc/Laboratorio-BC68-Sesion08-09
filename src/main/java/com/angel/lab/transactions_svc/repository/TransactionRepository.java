package com.angel.lab.transactions_svc.repository;
import com.angel.lab.transactions_svc.model.entities.Transaction;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;

public interface TransactionRepository extends ReactiveMongoRepository<Transaction, String> {
    Flux<Transaction> findByAccountIdOrderByTimestampDesc(String accountId);
}