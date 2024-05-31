package com.example.stocktrading.repository;

import com.example.stocktrading.model.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;


@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {

    @Query("SELECT COUNT(a) FROM Account a WHERE a.accountId = ?1")
    int countByAccountId(Long accountId);

    Account getAccountByAccountId(Long accountId);


    @Override
    <S extends Account> S save(S entity);
}
