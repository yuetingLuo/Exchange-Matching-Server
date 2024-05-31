package com.example.stocktrading.service;


import com.example.stocktrading.model.*;
import com.example.stocktrading.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;

@Service
public class OrderExecutor {

    private final AccountRepository accountRepository;
    private final AccountStockRepository accountStockRepository;
    private final OrderRepository orderRepository;
    private final ExecutionRepository executionRepository;
    private final CreateExecutor createExecutor;
    private final GlobalLock globalLock;

    @Autowired
    public OrderExecutor(AccountRepository accountRepository, AccountStockRepository accountStockRepository, OrderRepository orderRepository, ExecutionRepository executionRepository, CreateExecutor createExecutor, GlobalLock globalLock) {
        this.accountRepository = accountRepository;
        this.accountStockRepository = accountStockRepository;
        this.orderRepository = orderRepository;
        this.executionRepository = executionRepository;
        this.createExecutor = createExecutor;
        this.globalLock = globalLock;
    }

    public String createOrder(Long accountId, String sym, int share, double lim) {

        globalLock.acquireWriteLock();
        try {
            Order order;
            StringBuilder sb = new StringBuilder();
            int ashare = Math.abs(share);
            if (share < 0) {//sell
                AccountStock sellerHolds = accountStockRepository.findByAccountAccountIdAndSymbol(accountId, sym);
                Account sellerAccount = accountRepository.getAccountByAccountId(accountId);
                if (sellerHolds == null) {
                    String msg = "SYM invalid";
                    sb.append("<error>").append(msg)
                            .append("</error>\n");
                    return sb.toString();
                }
                if(sellerAccount == null) {
                    String msg = "AccountId invalid";
                    sb.append("<error>").append(msg)
                            .append("</error>\n");
                    return sb.toString();
                }
                if(sellerHolds.getShares() < ashare){
                    String msg = "Share insufficient, rejected";
                    sb.append("<error sym=\"").append(sym)
                            .append("\" amount=\"").append(share)
                            .append("\" limit=\"").append(lim)
                            .append("\">").append(msg)
                            .append("</error>\n");
                    return sb.toString();
                }else {
                    sellerHolds.setShares(sellerHolds.getShares() - ashare);
                    accountStockRepository.save(sellerHolds);//atomic
                }
                order = new Order("SELL", lim, ashare, "OPEN", sym, sellerAccount);
                orderRepository.save(order); //atomic

            } else {//buy
                double total_price = lim * share;
                Account buyerAccount = accountRepository.getAccountByAccountId(accountId);
                if(buyerAccount == null){
                    String msg = "AccountId invalid";
                    sb.append("<error>").append(msg)
                            .append("</error>\n");
                    return sb.toString();
                }
                if (buyerAccount.getBalance() < total_price) {
                    String msg = "Balance insufficient, rejected";
                    sb.append("<error sym=\"").append(sym)
                            .append("\" amount=\"").append(share)
                            .append("\" limit=\"").append(lim)
                            .append("\">").append(msg)
                            .append("</error>");
                    return sb.toString();
                } else {
                    buyerAccount.setBalance(buyerAccount.getBalance() - total_price);
                    accountRepository.save(buyerAccount);//atomic
                }
                order = new Order("BUY", lim, ashare, "OPEN", sym, buyerAccount);
                orderRepository.save(order); //atomic
            }
            transaction(order);//atomic
            sb.append("<opened sym=\"").append(sym)
                    .append("\" amount=\"").append(share)
                    .append("\" limit=\"").append(lim)
                    .append("\" id=\"").append(order.getOrderId())
                    .append("\"/>\n");

            return sb.toString();
        } finally {
            globalLock.releaseWriteLock();
        }

    }

    private void helper(Order newOrder, List<Order> ordersQueue) {
//        System.out.println("Enter into helper");
//        System.out.println(newOrder);
        while (newOrder.getRemainedShare() > 0 && ordersQueue.size() > 0) {
            Order existedOrder = ordersQueue.get(0);

            if (newOrder.getRemainedShare() <= existedOrder.getRemainedShare()) {
                int tranShare =  newOrder.getRemainedShare();
                newOrder.setRemainedShare(0);
                Execution exec1 = new Execution
                        (tranShare, existedOrder.getRequestedPrice(), newOrder);
                executionRepository.save(exec1);//atomic
                //update execution of buyer
                existedOrder.setRemainedShare(existedOrder.getRemainedShare() - tranShare);
                Execution exec2 = new Execution
                        (tranShare, existedOrder.getRequestedPrice(), existedOrder);
                executionRepository.save(exec2);//atomic

                updateOrders(newOrder, existedOrder, tranShare,newOrder.getSymbol());
                break;
            } else {
                //seller share greater than buyer
                int tranShare = existedOrder.getRemainedShare();
                newOrder.setRemainedShare(newOrder.getRemainedShare() - tranShare);
                Execution exec = new Execution
                        (tranShare, existedOrder.getRequestedPrice(), newOrder);
                executionRepository.save(exec);//atomic

                //update execution
                existedOrder.setRemainedShare(0);
                Execution exec2 = new Execution
                        (tranShare, existedOrder.getRequestedPrice(), existedOrder);
                executionRepository.save(exec2);//atomic

                updateOrders(newOrder, existedOrder, tranShare,newOrder.getSymbol());
                ordersQueue.remove(0);//remove the first one in queue
            }
        }
    }

    private void updateOrders(Order newOrder, Order existedOrder, int tranShare, String sym){
        if(Objects.equals(newOrder.getType(), "SELL")){
            createExecutor.addStock2Account(sym, existedOrder.getAccount().getAccountId(),tranShare);
            newOrder.getAccount().addBalance(tranShare * existedOrder.getRequestedPrice());
        }else{// new order is buy order
            createExecutor.addStock2Account(sym, newOrder.getAccount().getAccountId(),tranShare);
            if(newOrder.getRequestedPrice() > existedOrder.getRequestedPrice()){
                double refund = Math.abs((tranShare * (existedOrder.getRequestedPrice() - newOrder.getRequestedPrice())));
                newOrder.getAccount().addBalance(refund);
            }
            existedOrder.getAccount().addBalance(tranShare * existedOrder.getRequestedPrice());
        }
        accountRepository.save(newOrder.getAccount());//atomic
        accountRepository.save(existedOrder.getAccount());//atomic
        orderRepository.save(existedOrder);//atomic
        orderRepository.save(newOrder);//atomic
    }

    //order match
    private void transaction(Order order) {
        System.out.println("Enter into transaction");
        if (Objects.equals(order.getType(), "SELL")) {
            List<Order> buyerQueue = orderRepository.findBuyOrders
                    ("BUY", "OPEN", 0, order.getRequestedPrice());

            if (!buyerQueue.isEmpty()) {
                reorder(buyerQueue,1);
                System.out.println(buyerQueue);
                helper(order, buyerQueue);
            }
        }else if(Objects.equals(order.getType(), "BUY")){
            List<Order> sellerQueue = orderRepository.findSellOrders
                    ("SELL", "OPEN", 0, order.getRequestedPrice());

            if (!sellerQueue.isEmpty()) {
                reorder(sellerQueue,0);
                helper(order, sellerQueue);
            }
        }
    }

    private void reorder(List<Order> E, int stu) {
        if(stu == 1){
            // requested_price decadent
            E.sort(Comparator.comparingDouble(Order::getRequestedPrice).reversed()
                    .thenComparingLong(Order::getOrderId)); // orderId ascendant

        }else{//seller queue
            E.sort(Comparator.comparingDouble(Order::getRequestedPrice)
                    .thenComparingLong(Order::getOrderId));
        }
    }
}
