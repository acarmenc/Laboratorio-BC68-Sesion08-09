package com.angel.lab.transactions_svc.model.entities;


import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;

@Document("accounts")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Account {
    @Id
    private String id;
    private String number;
    private String holderName;
    private String currency; // "PEN" / "USD"
    private BigDecimal balance;
}