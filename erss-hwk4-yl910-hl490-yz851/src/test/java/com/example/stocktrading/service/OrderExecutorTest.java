package com.example.stocktrading.service;

import com.example.stocktrading.model.*;
import com.example.stocktrading.repository.*;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@SpringBootTest
@Transactional
public class OrderExecutorTest {

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private AccountStockRepository accountStockRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ExecutionRepository executionRepository;

    @Autowired
    private CreateExecutor createExecutor;

    @Autowired
    private OrderExecutor orderExecutor;


    @Test
    @Transactional
    void testTransaction() {
        Account a, b, c;
        double initBalance = 100000;
        createExecutor.createAccount(1L,0);
        createExecutor.createAccount(2L,0);
        createExecutor.createAccount(3L,initBalance);

        a = accountRepository.getAccountByAccountId(1L);
        b = accountRepository.getAccountByAccountId(2L);
        c = accountRepository.getAccountByAccountId(3L);

        c.setBalance(initBalance);

        createExecutor.addStock2Account("x", a.getAccountId(),100);
        createExecutor.addStock2Account("x", b.getAccountId(),100);

        orderExecutor.createOrder(b.getAccountId(),"x",-100,180);
        orderExecutor.createOrder(a.getAccountId(),"x",-100,150);

        assertEquals(accountStockRepository.findByAccountAccountIdAndSymbol
                (b.getAccountId(), "x").getShares(), 0);
        assertEquals(accountStockRepository.findByAccountAccountIdAndSymbol
                (a.getAccountId(), "x").getShares(), 0);

        List<Order> buyerOrders = orderRepository.findAll();

//        System.out.println(buyerOrders);
        assertTrue(buyerOrders.size() == 2);

        buyerOrders.forEach(order -> {
            assertEquals("SELL", order.getType());
            assertTrue(order.getRemainedShare() > 0);
        });

        String res = orderExecutor.createOrder(c.getAccountId(),"x",100,200);
        System.out.println("--------------------------------------------------");
        System.out.println(res);
        assertEquals(c.getBalance(), initBalance - 200 * 100 + 50 * 100, 0.01);
        assertEquals(a.getBalance(), 150 * 100, 0.01 );

        assertTrue(orderRepository.findAll().size() == 3);
        assertTrue(executionRepository.findAll().size() == 2);

        List<Execution> executionList = executionRepository.findAll();

        executionList.forEach(execution -> {
            assertTrue(execution.getExecuted_share() == 100);
            assertEquals(execution.getExecuted_price(),150.0, 0.01);
        });


    }


}
