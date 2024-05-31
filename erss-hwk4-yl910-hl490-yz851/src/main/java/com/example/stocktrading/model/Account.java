package com.example.stocktrading.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.*;


@Entity
@Table(name = "accounts")
@Data
@NoArgsConstructor
//@AllArgsConstructor

public class Account {
    @Id
    private Long accountId;
    private double balance;
//
//    @OneToMany(mappedBy = "account")//通过AccountStock类中名为account的字段来映射
//    private Set<AccountStock> accountStocks = new HashSet<>();

    public Account (Long accountId, double balance){
        this.accountId = accountId;
        this.balance = balance;
    }

    public void addBalance(double added){
        this.balance += added;
    }
}

