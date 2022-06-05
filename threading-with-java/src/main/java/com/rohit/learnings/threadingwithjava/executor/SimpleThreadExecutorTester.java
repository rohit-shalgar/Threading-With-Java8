package com.rohit.learnings.threadingwithjava.executor;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class SimpleThreadExecutorTester {
    public static void main(String[] args) throws InterruptedException {

        BlockingDeque<Runnable> threadQueue = new LinkedBlockingDeque<>(5);

        final AtomicInteger counter = new AtomicInteger(1);
        ThreadFactory threadFactory = (Runnable r) -> {
            System.out.println("Createing a new thread 'Cool-Thread- " + "' " + counter.incrementAndGet() + "'");
            return new Thread(r, "Cool-Thread-" + counter.get());
        };

        RejectedExecutionHandler rejectedExecutionHandler =
                (Runnable r, ThreadPoolExecutor executor) -> {
                    if (r instanceof SimpleThreadExecutor simpleThreadExecutor) {
                        System.out.println("Rejecting the task " + simpleThreadExecutor.getTaskId());
                    }
                };


        ThreadPoolExecutor executor = new ThreadPoolExecutor(10,
                20,
                1,
                TimeUnit.SECONDS,
                threadQueue,
                threadFactory,
                rejectedExecutionHandler);

        for (int i = 0; i <= 50; i++) {
            executor.execute(new SimpleThreadExecutor(i));
        }

        executor.shutdown();
        executor.awaitTermination(Integer.MAX_VALUE, TimeUnit.MILLISECONDS);

    }
}
