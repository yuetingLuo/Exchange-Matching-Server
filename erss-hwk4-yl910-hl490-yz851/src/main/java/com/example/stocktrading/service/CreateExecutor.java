package com.example.stocktrading.service;

import com.example.stocktrading.model.*;
import com.example.stocktrading.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
public class CreateExecutor {
    private final AccountRepository accountRepository;
    private final AccountStockRepository accountStockRepository;
    private final GlobalLock globalLock;

    @Autowired
    public CreateExecutor(AccountRepository accountRepository, AccountStockRepository accountStockRepository, GlobalLock globalLock) {
        this.accountRepository = accountRepository;
        this.accountStockRepository = accountStockRepository;
        this.globalLock = globalLock;
    }

    public String createAccount(Long accountId, double balance){
        globalLock.acquireWriteLock();
        try {
            // Check whether the AccountId exists.
            StringBuilder sb = new StringBuilder();
            if(accountRepository.countByAccountId(accountId) > 0){
                String msg = "The account ID exists";
                sb.append("<error id=\"").append(accountId)
                        .append("\">").append(msg)
                        .append("</error>\n");
            } else {
                Account account = new Account(accountId, balance);
                accountRepository.save(account);
                sb.append("<created id=\"").append(accountId).append("\"/>\n");
            }
            return sb.toString();
        } finally {
            globalLock.releaseWriteLock();
        }

    }

    public String addStock2Account(String symbol, Long accountId, int shares){
        globalLock.acquireWriteLock();
        try {
            //Check whether the account already has this stock.
            StringBuilder sb = new StringBuilder();
            AccountStock existingAccountStock = accountStockRepository.findByAccountAccountIdAndSymbol
                    (accountId, symbol);
            if(existingAccountStock != null){
                int newShares = shares + existingAccountStock.getShares();
                existingAccountStock.setShares(newShares);
                accountStockRepository.save(existingAccountStock); //atomic
            } else if(accountRepository.countByAccountId(accountId) == 0){
                String msg = "The account does not exist";
                sb.append("<error sym=\"").append(symbol)
                        .append("\" id=\"").append(accountId)
                        .append("\">").append(msg)
                        .append("</error>\n");
                return sb.toString();
            }else{
                Account curAccount = accountRepository.getAccountByAccountId(accountId);
                AccountStock accountStock = new AccountStock(curAccount, symbol, shares);
                accountStockRepository.save(accountStock);//atomic
            }
            sb.append("<created sym=\"").append(symbol)
                    .append("\" id=\"").append(accountId)
                    .append("\"/>\n");

            return sb.toString();
        } finally {
            globalLock.releaseWriteLock();
        }
    }
}
