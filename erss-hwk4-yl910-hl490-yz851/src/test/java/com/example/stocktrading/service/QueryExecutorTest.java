package com.example.stocktrading.service;

import com.example.stocktrading.model.*;
import com.example.stocktrading.repository.AccountRepository;
import com.example.stocktrading.repository.ExecutionRepository;
import com.example.stocktrading.repository.OrderRepository;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
public class QueryExecutorTest {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private ExecutionRepository executionRepository;

    @Autowired
    private QueryExecutor queryExecutor;

    @Test
    @Transactional
    public void testQueryWithOpenOrder() {

        Order mockOrder = new Order();
        mockOrder.setStatus("OPEN");
        mockOrder.setRemainedShare(100);
        Account a = new Account(1L,1000);
        accountRepository.save(a);
        mockOrder.setAccount(a);
        orderRepository.save(mockOrder);
        String expected = "<status id=\"1\">\n" +
                "    <open shares=\"100\"/>\n" +
                "</status>\n";
        String result = queryExecutor.query(1L, 1L);
        assertEquals(expected, result);
    }

    @Test
    public void testQueryWithInvalidTransactionId() {
        String expected = "<status id=\"3\">\n" +
                "    <error>TransactionID is invalid</error>\n" +
                "</status>\n";
        String result = queryExecutor.query(1L, 3L);
        assertEquals(expected, result);
    }
}
