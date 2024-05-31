package com.example.stocktrading.repository;


import com.example.stocktrading.model.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AccountStockRepository extends JpaRepository<AccountStock, Long> {
     // Find AccountStock based on accountId and symbol
    AccountStock findByAccountAccountIdAndSymbol(Long accountId, String symbol);

    @Override
    <S extends AccountStock> S save(S entity);

}