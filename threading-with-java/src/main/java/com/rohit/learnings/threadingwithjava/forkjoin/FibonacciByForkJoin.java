/*
    FOrk/JOIN framework breaks bigger tasks into smaller ones, and joins the result at the end.
 */


package com.rohit.learnings.threadingwithjava.forkjoin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveAction;
import java.util.concurrent.RecursiveTask;
import java.util.logging.Logger;

//Recursive task is fork join task to compute the action recursively. Also Recursive task can be used to perform a task recursively.
public class FibonacciByForkJoin extends RecursiveAction {

    private static final Logger LOGGER = Logger.getLogger(FibonacciByForkJoin.class.getName());

    private static final int threshold = 5; // if the INPUT number for fib series calculation is great than this, we will invoke fork join.

    private long fibNumber;



    public FibonacciByForkJoin(Long fibNumber) {
        this.fibNumber = fibNumber;
    }


    private long fibonacciNumber(long n) {

        if (n <= 1) {
            return n;
        }
        return fibonacciNumber(n - 1) + fibonacciNumber(n - 2);
    }

    @Override
    protected void compute() {
        final long n = fibNumber;
        if (n <= threshold) {
            fibNumber = fibonacciNumber(n);
        } else {
            fibNumber = ForkJoinTask.invokeAll(createTasks(n))
                    .stream()
                    .mapToLong(FibonacciByForkJoin::fibonacciNumber)
                    .sum();
        }
    }

    private List<FibonacciByForkJoin> createTasks(long n) {
        final List<FibonacciByForkJoin> tasks = new ArrayList<>();
        FibonacciByForkJoin fibonacciByForkJoinMinusOne = new FibonacciByForkJoin(n - 1);
        FibonacciByForkJoin fibonacciByForkJoinMinusTwo = new FibonacciByForkJoin(n - 2);
        tasks.add(fibonacciByForkJoinMinusOne);
        tasks.add(fibonacciByForkJoinMinusTwo);

        return tasks;

    }

    public long fibonacciNumber() {
        return fibNumber;
    }
}
