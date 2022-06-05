package com.rohit.learnings.threadingwithjava.callableandfuture;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class Utilities {

    private static final ExecutorService executorService = Executors.newSingleThreadExecutor();

    //1- canceling a future
    public static void cancelAFuture() throws InterruptedException {
        final long startTime = System.currentTimeMillis();
        Future<Object> future = executorService.submit(() -> {

            Thread.sleep(3000);

            return "Task completed";
        });
        while (!future.isDone()) {
            System.out.println("Future is not done yet!");
            Thread.sleep(100);
            final long elapsedTime = System.currentTimeMillis() - startTime;
            if (elapsedTime > 1000) {
                System.out.println("Cancelled the future, bye!");
                future.cancel(true);
                System.exit(0);
            }
        }
    }

    //Executors.callable(lambda) will return a callable on which we can run submit() to get future.
}
