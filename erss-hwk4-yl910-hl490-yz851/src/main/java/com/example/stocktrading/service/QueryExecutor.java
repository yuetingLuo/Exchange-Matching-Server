package com.example.stocktrading.service;

import com.example.stocktrading.model.Account;
import com.example.stocktrading.model.Execution;
import com.example.stocktrading.model.Order;
import com.example.stocktrading.repository.AccountRepository;
import com.example.stocktrading.repository.ExecutionRepository;
import com.example.stocktrading.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
public class QueryExecutor {

    private final OrderRepository orderRepository;
    private final ExecutionRepository executionRepository;
    private final AccountRepository accountRepository;
    private final GlobalLock globalLock;

    @Autowired
    public QueryExecutor(OrderRepository orderRepository, ExecutionRepository executionRepository, AccountRepository accountRepository, GlobalLock globalLock) {
        this.orderRepository = orderRepository;
        this.executionRepository = executionRepository;
        this.accountRepository = accountRepository;
        this.globalLock = globalLock;
    }

    public String query(Long accountId, Long orderId){
        globalLock.acquireReadLock();
        try {

            StringBuilder sb = new StringBuilder();
            Order order = orderRepository.getOrderByOrderId(orderId);
            Account account = accountRepository.getAccountByAccountId(accountId);
            sb.append("<status id=\"").append(orderId).append("\">\n");
            String indent = "    ";
            if(order == null){
                String msg = "TransactionID is invalid";
                sb.append(indent).append("<error>").append(msg)
                        .append("</error>\n");
                sb.append("</status>\n");
                return sb.toString();
            }

            if(account == null){
                String msg = "AccountId invalid";
                sb.append(indent).append("<error>").append(msg)
                        .append("</error>\n");
                sb.append("</status>\n");
                return sb.toString();
            }
            if(!Objects.equals(account.getAccountId(), order.getAccount().getAccountId())){
                String msg = "This transaction is not for you";
                sb.append(indent).append("<error>").append(msg)
                        .append("</error>\n");
                sb.append("</status>\n");
                return sb.toString();
            }

            if(Objects.equals(order.getStatus(), "OPEN")){
                if(order.getRemainedShare() != 0){
                    sb.append(indent).append("<open shares=\"").append(order.getRemainedShare())
                            .append("\"/>\n");
                }
            }else{
                sb.append(indent).append("<canceled shares=\"").append(order.getRemainedShare())
                        .append("\" time=\"").append(order.getCancelTime())
                        .append("\"/>\n");
            }

            List<Execution> executionList = executionRepository.findByOrderOrderId(orderId);
            for(Execution e: executionList){
                sb.append(indent).append("<executed shares=\"").append(e.getExecuted_share())
                        .append("\" price=\"").append(e.getExecuted_price())
                        .append("\" time=\"").append(e.getEpochSeconds())
                        .append("\"/>\n");
            }
            sb.append("</status>\n");

            return sb.toString();
        } finally {
            globalLock.releaseReadLock();
        }
    }
}
