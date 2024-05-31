package com.example.stocktrading.model;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import javax.persistence.*;
import java.time.Instant;


@Entity
@Table(name = "executions")
@Data
@NoArgsConstructor


public class Execution {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long executionId;
    private int executed_share;
    private double executed_price;
    private long epochSeconds;

    @ManyToOne
    @JoinColumn(name = "order_id")
    private Order order;

    public Execution (int share, double price, Order order){
        this.executed_share = share;
        this.executed_price = price;
        this.order = order;
        this.epochSeconds = Instant.now().getEpochSecond();
    }
}
