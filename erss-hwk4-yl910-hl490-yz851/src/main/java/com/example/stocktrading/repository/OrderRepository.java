package com.example.stocktrading.repository;

import com.example.stocktrading.model.*;
import com.example.stocktrading.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    Order getOrderByOrderId(Long orderId);

    @Query("SELECT o FROM Order o WHERE o.type = :type AND o.status = :status " +
            "AND o.remainedShare > :remainedShare AND o.requestedPrice >= :price")
    List<Order> findBuyOrders
            (@Param("type") String type, @Param("status") String status,
             @Param("remainedShare") int remainedShare, @Param("price") double price);


    @Query("SELECT o FROM Order o WHERE o.type = :type AND o.status = :status " +
            "AND o.remainedShare > :remainedShare AND o.requestedPrice <= :price")
    List<Order> findSellOrders
            (@Param("type") String type, @Param("status") String status,
             @Param("remainedShare") int remainedShare, @Param("price") double price);


    @Override
    <S extends Order> S save(S entity);
}
