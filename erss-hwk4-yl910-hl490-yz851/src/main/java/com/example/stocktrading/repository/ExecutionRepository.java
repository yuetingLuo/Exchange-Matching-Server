package com.example.stocktrading.repository;

import com.example.stocktrading.model.*;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExecutionRepository extends JpaRepository<Execution, Long> {

    List<Execution> findByOrderOrderId(Long orderId);
    @Override
    <S extends Execution> S save(S entity);
}
