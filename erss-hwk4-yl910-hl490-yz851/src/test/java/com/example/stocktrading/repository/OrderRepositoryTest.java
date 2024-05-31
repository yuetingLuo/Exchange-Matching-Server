package com.example.stocktrading.repository;

import com.example.stocktrading.model.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import javax.transaction.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@DataJpaTest
class OrderRepositoryTest {

    @Autowired
    private OrderRepository orderRepository;

    @Test
    @Transactional
    public void testGetOrderByOrderId() {

        Order order = new Order();

        order.setType("BUY");
        order.setStatus("OPEN");
        order.setRemainedShare(100);
        order.setRequestedPrice(50.0);
        order = orderRepository.save(order);

        Order foundOrder = orderRepository.getOrderByOrderId(order.getOrderId());
        assertNotNull(foundOrder);
        assertEquals(order.getOrderId(), foundOrder.getOrderId());
    }


    @Test
    @Transactional
    public void testFindBuyOrders() {

        createOrder("BUY", "OPEN", 100, 50.0);
        createOrder("BUY", "OPEN", 50, 40.0);
        createOrder("SELL", "OPEN", 100, 55.0);

        List<Order> buyOrders = orderRepository.findBuyOrders("BUY", "OPEN", 0, 45.0);

        assertNotNull(buyOrders);
        assertFalse(buyOrders.isEmpty());
        assertTrue(buyOrders.size() >= 1);
        buyOrders.forEach(order -> {
            assertEquals("BUY", order.getType());
            assertTrue(order.getRemainedShare() > 0);
            assertTrue(order.getRequestedPrice() >= 45.0);
        });
    }

    @Test
    @Transactional
    public void testFindSellOrders() {

        createOrder("SELL", "OPEN", 100, 50.0);
        createOrder("BUY", "OPEN", 50, 40.0);
        createOrder("SELL", "OPEN", 100, 35.0);

        List<Order> sellOrders = orderRepository.findSellOrders("SELL", "OPEN", 0, 45.0);

        assertNotNull(sellOrders);
        assertFalse(sellOrders.isEmpty());
        assertTrue(sellOrders.size() >= 1);
        sellOrders.forEach(order -> {
            assertEquals("SELL", order.getType());
            assertTrue(order.getRemainedShare() > 0);
            assertTrue(order.getRequestedPrice() <= 45.0);
        });
    }

    private void createOrder(String type, String status, int remainedShare, double requestedPrice) {
        Order order = new Order();
        order.setType(type);
        order.setStatus(status);
        order.setRemainedShare(remainedShare);
        order.setRequestedPrice(requestedPrice);
        orderRepository.save(order);
    }
}