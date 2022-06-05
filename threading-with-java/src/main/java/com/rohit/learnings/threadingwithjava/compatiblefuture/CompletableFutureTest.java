package com.rohit.learnings.threadingwithjava.compatiblefuture;

import java.util.concurrent.ExecutionException;



public class CompletableFutureTest {

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        CompletableFutureBasics.printOrder();
        System.out.println("Customer :"+CompletableFutureBasics.fetchCustomerSummaryExecutor());
        System.out.println("Invoice is :"+CompletableFutureBasics.fetchInvoiceTotalSign());
    }
}
