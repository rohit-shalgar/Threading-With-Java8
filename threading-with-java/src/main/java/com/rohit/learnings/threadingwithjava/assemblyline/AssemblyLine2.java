package com.rohit.learnings.threadingwithjava.assemblyline;


//Producer is faster than consumer and does not wait.
import java.util.Random;
import java.util.concurrent.*;
import java.util.logging.Logger;

public final class AssemblyLine2 {

    private static final Logger LOGGER = Logger.getLogger(AssemblyLine2.class.getName());

    private static final Producer producer = new Producer();
    private static final Consumer consumer = new Consumer();

    private static ExecutorService producerService;
    private static ExecutorService consumerService;

    private static BlockingQueue<String> queue = new LinkedBlockingQueue<>();


    private AssemblyLine2() {
        throw new AssertionError("There is a single assembly line.");
    }

    private static final int MAX_PROD_TIME_MS = 5 * 1000;
    private static final int MAX_CONS_TIME_MS = 7 * 1000;
    private static final int TIMEOUT_MS = MAX_CONS_TIME_MS + 1000;
    private static final Random rnd = new Random();
    private static volatile boolean runningProducer;

    private static class Producer implements Runnable {

        @Override
        public void run() {
            while (runningProducer) {
                String bulb = "Bulb-" + rnd.nextInt(1000);
                try {
                    Thread.sleep(rnd.nextInt(MAX_PROD_TIME_MS));
                    boolean isTransferred = queue.offer(bulb, TIMEOUT_MS, TimeUnit.MILLISECONDS);
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
        producerService = Executors.newSingleThreadExecutor();
        producerService.execute(producer);

        runningConsumer = true;
        consumerService = Executors.newSingleThreadExecutor();
        consumerService.execute(consumer);

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
            if (!executorService.awaitTermination(TIMEOUT_MS * 2, TimeUnit.MILLISECONDS)) {
                executorService.shutdownNow();
                return executorService.awaitTermination(TIMEOUT_MS * 2, TimeUnit.MILLISECONDS);
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
