package com.example.stocktrading.service;

import com.example.stocktrading.model.Account;
import com.example.stocktrading.model.Execution;
import com.example.stocktrading.model.Order;
import com.example.stocktrading.repository.*;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

@Service
public class CancelExecutor {

    private final OrderRepository orderRepository;
    private final AccountStockRepository accountStockRepository;
    private final AccountRepository accountRepository;
    private final CreateExecutor createExecutor;
    private final ExecutionRepository executionRepository;
    private final GlobalLock globalLock;

    public CancelExecutor(OrderRepository orderRepository,
                          AccountStockRepository accountStockRepository, AccountRepository accountRepository, CreateExecutor createExecutor, ExecutionRepository executionRepository, GlobalLock globalLock) {
        this.orderRepository = orderRepository;
        this.accountStockRepository = accountStockRepository;
        this.accountRepository = accountRepository;
        this.createExecutor = createExecutor;
        this.executionRepository = executionRepository;
        this.globalLock = globalLock;

    }

    public String cancel(Long orderId, Long accountId){
        globalLock.acquireWriteLock();
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("<canceled id=\"").append(orderId).append("\">\n");
            String indent = "    ";
            Order order = orderRepository.getOrderByOrderId(orderId);
            if(order == null){
                String msg = "TransactionID invalid";
                sb.append(indent).append("<error>").append(msg)
                        .append("</error>\n");
            }else if(order.getRemainedShare() == 0){
                String msg = "The order has been completed";
                sb.append(indent).append("<error>").append(msg)
                        .append("</error>\n");
            }else if(Objects.equals(order.getStatus(), "CANCEL")){
                String msg = "The order has been canceled";
                sb.append(indent).append("<error>").append(msg)
                        .append("</error>\n");
            }else if(!Objects.equals(accountId, order.getAccount().getAccountId())){
                String msg = "This transaction is not for you";
                sb.append(indent).append("<error>").append(msg)
                        .append("</error>\n");
                return sb.toString();
            }else{
                if(Objects.equals(order.getType(), "BUY")){
                    double refund = order.getRemainedShare() * order.getRequestedPrice();
                    Account account = order.getAccount();
                    account.addBalance(refund);
                    accountRepository.save(account);//atomic

                }else{
                    createExecutor.addStock2Account
                            (order.getSymbol(), order.getAccount().getAccountId(), order.getRemainedShare());
                }

                order.setStatus("CANCEL");
                order.setCancelTime(Instant.now().getEpochSecond());
                orderRepository.save(order);

                //response to server
                sb.append(indent).append("<canceled shares=\"").append(order.getRemainedShare())
                        .append("\" time=\"").append(order.getCancelTime())
                        .append("\"/>\n");

                List<Execution> executionList = executionRepository.findByOrderOrderId(orderId);
                for(Execution e: executionList){
                    sb.append(indent).append("<executed shares=\"").append(e.getExecuted_share())
                            .append("\" price=\"").append(e.getExecuted_price())
                            .append("\" time=\"").append(e.getEpochSeconds())
                            .append("\"/>\n");
                }
            }
            sb.append("</canceled>\n");
            return sb.toString();
        } finally {
            globalLock.releaseWriteLock();
        }

    }
}
