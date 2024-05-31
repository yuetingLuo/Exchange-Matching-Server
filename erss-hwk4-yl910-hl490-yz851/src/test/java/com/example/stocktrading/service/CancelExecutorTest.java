package com.example.stocktrading.service;

import com.example.stocktrading.model.*;
import com.example.stocktrading.repository.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@Transactional
class CancelExecutorTest {
    @Autowired
    private CancelExecutor cancelExecutor;

    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private ExecutionRepository executionRepository;
    @Autowired
    private AccountRepository accountRepository;

    @Test
    @Transactional
    public void testCancelBuyOrder() {
        Long orderId = 1L;
        Account account = new Account();
        account.setAccountId(1L);
        account.setBalance(1000.0);

        Order order = new Order();
        order.setStatus("open");
        order.setType("buy");
        order.setRequestedPrice(10.0);
        order.setRemainedShare(50);
        order.setAccount(account);
        accountRepository.save(account);
        orderRepository.save(order);

        // Execute the cancel method
        String result = cancelExecutor.cancel(orderId, 1L);
        order = orderRepository.getOrderByOrderId(orderId);
        // Verify the outcome
        assertEquals("<canceled id=\"1\">\n" +
                "    <canceled shares=\"50\" time=\"" + order.getCancelTime() + "\"/>\n" +
                "</canceled>\n", result);

    }

}