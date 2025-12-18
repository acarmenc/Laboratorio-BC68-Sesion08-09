package com.angel.lab.transactions_svc.repository;

import com.angel.lab.transactions_svc.model.entities.Account;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Mono;

public interface AccountRepository extends ReactiveMongoRepository<Account,String>
{
    Mono<Account> findByNumber(String number);
}
