package com.angel.lab.transactions_svc.repository;

import com.angel.lab.transactions_svc.model.entities.RiskRule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RiskRuleRepository extends JpaRepository<RiskRule, Long> {
    Optional<RiskRule> findFirstByCurrency(String currency);
}
