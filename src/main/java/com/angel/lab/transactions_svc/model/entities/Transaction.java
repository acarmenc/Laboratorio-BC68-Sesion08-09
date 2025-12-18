package com.angel.lab.transactions_svc.model.entities;

import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.Instant;

@Document("transactions")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Transaction {
    @Id
    private String id;
    private String accountId;
    private String type; // "CREDIT" or "DEBIT"
    private BigDecimal amount;
    private Instant timestamp;
    private String status; // "OK" or "REJECTED"
    private String reason; // null if OK
}
