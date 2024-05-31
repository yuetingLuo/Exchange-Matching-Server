package com.example.stocktrading.model;

import javax.persistence.*;
import lombok.*;

@Entity
@Table(name = "account_stocks", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"account_id", "symbol"})
})
@Data
@NoArgsConstructor
@AllArgsConstructor

public class AccountStock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "account_id")
    private Account account;

    private String symbol;

    private int shares;

    public AccountStock(Account account, String symbol, int shares){
        this.account = account;
        this.symbol = symbol;
        this.shares = shares;
    }
}
