package com.example.stocktrading.model;

import javax.persistence.*;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Table(name = "orders")
@Data
@NoArgsConstructor

public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long orderId;
    private String type;//buy sell
    private double requestedPrice;
    private int remainedShare;
    private String status;//cancel open
    private String symbol;

    private long cancelTime;

    @OneToOne
    @JoinColumn(name = "account_id")
    private Account account;

    public Order(String type, double requestedPrice, int remainedShare, String status, String symbol, Account account){
        this.account = account;
        this.remainedShare = remainedShare;
        this.status = status;
        this.type = type;
        this.requestedPrice = requestedPrice;
        this.symbol = symbol;
    }

}
