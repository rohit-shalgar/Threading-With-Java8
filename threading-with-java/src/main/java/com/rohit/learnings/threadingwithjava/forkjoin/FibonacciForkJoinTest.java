package com.rohit.learnings.threadingwithjava.forkjoin;

import java.util.concurrent.ForkJoinPool;

public class FibonacciForkJoinTest {

    public static void main(String[] args) {
        ForkJoinPool pool = ForkJoinPool.commonPool();
        FibonacciByForkJoin fibonacci = new FibonacciByForkJoin(15L);
        pool.invoke(fibonacci);

        System.out.println(fibonacci.fibonacciNumber());
    }
}
