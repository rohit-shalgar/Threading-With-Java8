package com.rohit.learnings.threadingwithjava.compatiblefuture;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/*
    Completable future - JDK 8+ upgrade of futures
        - callback, exception handling - exceptionally() block, can not be explicitly complete.- can call complete and return some default value if not complete.
        - threads are obtained from ForkJoin.commonpool()
        - can be chained with thenApply - apply some function and return value,
        - thenAccept() - return void,
        - thenRun() - run a function
        - these are run on same thread, we have thenAcceptAsync() etc ... which will run in different thread from forkjoin.commonpool
            or on from executor pool.
        - handle() - callback will act like a finally block.
 */
public class CompletableFutureBasics {

    public static void printOrder() throws ExecutionException, InterruptedException {
        CompletableFuture<Void> completableFuture = CompletableFuture.runAsync(
                () -> {
                    System.out.println("Order was printed by -" + Thread.currentThread().getName());
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }

                }
        );
        completableFuture.get();
    }

    public static String fetchCustomerSummaryExecutor() throws ExecutionException, InterruptedException {
        ExecutorService service = Executors.newSingleThreadExecutor();
        CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> {
            System.out.println("Order summary was fetched by -" + Thread.currentThread().getName());
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            return "Order execute #12456";
        }, service);
        String summary = future.get();
        service.shutdown();
        return summary;
    }

    //fetch invoice number, attach total and sign it.
    public static String fetchInvoiceTotalSign() throws ExecutionException, InterruptedException {
        CompletableFuture<String> invoiceService = CompletableFuture.supplyAsync(
                        () -> {
                            System.out.println("Invoice is being fetched by -" + Thread.currentThread().getName());
                            try {
                                Thread.sleep(1000);
                            } catch (InterruptedException e) {
                                Thread.currentThread().interrupt();// this insures the state change.
                            }
                            return "Invoice #3421";
                        }
                )
                .thenApply(invoice -> invoice.concat("134$")).exceptionally(
                        (ex)-> "This is how you handle exceptions"
                )
                .thenApply(invoice -> invoice.concat("Signed"));

        return invoiceService.get();

    }

}
