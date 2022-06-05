package com.rohit.learnings.threadingwithjava.assemblyline;


//3 Producers is and 2 consumers working in parallel - fixed number of threads.

import java.util.Random;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public final class AssemblyLineMultiplePC {

    private static final Logger LOGGER = Logger.getLogger(AssemblyLineMultiplePC.class.getName());

    private static final int PRODUCERS = 3;
    private static final int CONSUMERS = 2;
    private static final Producer producer = new Producer();
    private static final Consumer consumer = new Consumer();

    private static ExecutorService producerService;
    private static ExecutorService consumerService;

    private static final ConcurrentLinkedQueue<String> queue = new ConcurrentLinkedQueue<>();


    private AssemblyLineMultiplePC() {
        throw new AssertionError("There is a single assembly line.");
    }

    private static final int MAX_PROD_TIME_MS = 2 * 1000;
    private static final int MAX_CONS_TIME_MS = 2 * 1000;
    private static final int TIMEOUT_MS = (MAX_CONS_TIME_MS + MAX_PROD_TIME_MS) * (PRODUCERS+CONSUMERS);
    private static final Random rnd = new Random();
    private static volatile boolean runningProducer;

    private static class Producer implements Runnable {

        @Override
        public void run() {
            while (runningProducer) {
                String bulb = "Bulb-" + rnd.nextInt(1000);
                try {
                    Thread.sleep(rnd.nextInt(MAX_PROD_TIME_MS));
                    boolean isTransferred = queue.offer(bulb);
                    if (isTransferred) {
                        LOGGER.info("transferred-" + bulb);
                    }

                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    LOGGER.severe("Exception:" + e);
                    break;
                }
            }


        }
    }

    private static volatile boolean runningConsumer;

    private static class Consumer implements Runnable {

        @Override
        public void run() {
            while (runningConsumer) {
                String bulb = null;
                try {
                    bulb = queue.poll();
                    if (bulb != null) {

                        Thread.sleep(rnd.nextInt(MAX_CONS_TIME_MS));
                        LOGGER.info("Packed -" + bulb);
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    LOGGER.severe("Exception:" + e);
                    break;
                }
            }

        }


    }


    public static void startAssemblyLine() {
        if (runningConsumer || runningProducer) {
            LOGGER.info("Assembly line is already running");
            return;
        }
        LOGGER.info("\n\n Starting Assembly line...");
        LOGGER.info("\n\n Remaining bulbs in the queue - " + queue + "\n\n");

        runningProducer = true;
        producerService = Executors.newFixedThreadPool(PRODUCERS);
        for (int i = 0; i < PRODUCERS; i++) {
            producerService.execute(producer);
        }

        runningConsumer = true;
        consumerService = Executors.newFixedThreadPool(CONSUMERS);
        for (int i = 0; i < CONSUMERS; i++) {
            producerService.execute(consumer);
        }


    }

    public static void shutDownAssemblyLine() {
        LOGGER.info("Shutting down the assembly");

        boolean isProducerShutDown = shutDownProducer();
        boolean isConsumerShutDown = shutDownConsumer();

        if (!isConsumerShutDown || !isProducerShutDown) {
            LOGGER.severe("Assembly shutdown abruptly....");
            System.exit(0);
        }
        LOGGER.info("Assembling line was successfully stopped!");
    }

    private static boolean shutDownConsumer() {
        runningConsumer = false;
        return shutDownExecutorService(consumerService);
    }

    private static boolean shutDownProducer() {
        runningProducer = false;
        return shutDownExecutorService(producerService);
    }

    private static boolean shutDownExecutorService(ExecutorService executorService) {
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(TIMEOUT_MS , TimeUnit.MILLISECONDS)) {
                executorService.shutdownNow();
                return executorService.awaitTermination(TIMEOUT_MS, TimeUnit.MILLISECONDS);
            }
            return true;
        } catch (InterruptedException e) {
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
            LOGGER.severe(() -> "Exception: " + e);
        }
        return false;
    }

}
