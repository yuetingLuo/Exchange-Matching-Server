package com.example.stocktrading.repository;

import com.example.stocktrading.model.Account;
import com.example.stocktrading.model.AccountStock;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;
import org.springframework.boot.test.context.SpringBootTest;



import javax.transaction.Transactional;


@SpringBootTest
@Transactional
class AccountStockRepositoryTest {

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private AccountStockRepository accountStockRepository;

    @Test
    public void testAddStockToAccount() {

        Account account = new Account();
        account.setAccountId(1122334455L);
        accountRepository.save(account);
        String symbol = "xyz";

        // 将Stock添加到Account中
        AccountStock accountStock = new AccountStock();
        accountStock.setAccount(account);
        accountStock.setSymbol(symbol);
        accountStock.setShares(100);
        accountStockRepository.save(accountStock);

        assertThat(accountStockRepository.findByAccountAccountIdAndSymbol(account.getAccountId(), symbol)).isNotNull();
        System.out.println(accountStockRepository.findByAccountAccountIdAndSymbol(account.getAccountId(),symbol));
    }

}