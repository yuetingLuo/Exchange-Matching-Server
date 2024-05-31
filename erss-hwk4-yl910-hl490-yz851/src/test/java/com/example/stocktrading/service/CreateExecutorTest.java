package com.example.stocktrading.service;

import com.example.stocktrading.repository.AccountRepository;
import com.example.stocktrading.repository.AccountStockRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;


import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class CreateExecutorTest {
    @Autowired
    private AccountRepository accountRepository;
    @Autowired
    private CreateExecutor createExecutor;

    @Autowired
    private AccountStockRepository accountStockRepository;

    @Test
    public void testCreateAccount(){
        Long id = 1122334455L;
        double balance = 1000;
        createExecutor.createAccount(id,balance);
        assertEquals(balance, accountRepository.findById(id).get().getBalance(), 0.01);

        double balance2 = 2000;
        createExecutor.createAccount(id,balance2);
        assertEquals(accountRepository.getById(id).getBalance(),balance);

        Long id2 = 2233445566L;
        createExecutor.createAccount(id2,balance2);
        assertEquals(accountRepository.getById(id).getBalance(),balance);

    }

    @Test
    public void testAddStock2AccountwoaAccount(){
        Long id = 1122334455L;
        double balance = 1000;
        int share = 1000;
        String sym = "abc";
        assertEquals(accountRepository.countByAccountId(id),0);

    }

    @Test
    public void testAddStock2AccountAccount(){
        Long id = 1122334455L;
        double balance = 1000;
        int share = 1000;
        String sym = "abc";
        createExecutor.createAccount(id,balance);
        assertEquals(balance, accountRepository.findById(id).get().getBalance(), 0.01);

        String res = createExecutor.addStock2Account(sym,id,share);
        System.out.println("--------------------------------------------------");
        System.out.println(res);
        assertEquals(accountStockRepository.findByAccountAccountIdAndSymbol(id,sym).getShares(), share);

        createExecutor.addStock2Account(sym,id,share);
        share += share;
        assertEquals(accountStockRepository.findByAccountAccountIdAndSymbol(id,sym).getShares(), share);

    }
}