package com.rohit.learnings.threadingwithjava.callableandfuture;

/*
    Callable - interface with call() method which can return some output and can throw exceptions as well unlike run() in runnable
    Callable.call() - returns a future, a promise that task will be completed in the future asynchronously.
    or EXECUTOR.SUBMIT()
    we can perform other tasks while future.done() returns true

 */

//Automated system which -
/*
    An automatic system sends a request to the producer, saying, check this bulb and if it is ok then return it to me,
    otherwise tell me what went wrong with this bulb.
    The automatic system waits for the producer to check the bulb.
    When the automatic system receives the checked bulb, it is then passed further to the consumer (packer) and repeats the process.
    If a bulb has a defect, the producer throws an exception (DefectBulbException)
    and the automatic system will inspect the cause of the problem.
 */

import com.rohit.learnings.threadingwithjava.callableandfuture.exception.FaultyBulbException;

import java.util.Random;
import java.util.concurrent.*;
import java.util.logging.Logger;

public final class AutomatedAssemblyLine {

    private static final Logger LOGGER = Logger.getLogger(AutomatedAssemblyLine.class.getName());

    private static ExecutorService producerService;
    private static ExecutorService consumerService;


    private AutomatedAssemblyLine() {
        throw new AssertionError("There is a single assembly line.");
    }

    private static final int MAX_PROD_TIME_MS = 5 * 1000;
    private static final int MAX_CONS_TIME_MS = 3 * 1000;
    private static final int TIMEOUT_MS = MAX_CONS_TIME_MS + MAX_PROD_TIME_MS + 1000;
    private static final Random rnd = new Random();
    private static volatile boolean runningProducer;

    private static class Producer implements Callable {

        private final String bulb;

        public Producer(String bulb) {
            this.bulb = bulb;
        }


        @Override
        public String call() throws Exception {
            if (runningProducer) {
                Thread.sleep(rnd.nextInt(MAX_PROD_TIME_MS));
                if (rnd.nextInt(100) < 5) {
                    throw new FaultyBulbException("Defective bulb -" + bulb);
                } else {
                    LOGGER.info("Checked the bulb -" + bulb);
                }
                return bulb;
            }
            return "";
        }
    }

    private static volatile boolean runningConsumer;

    private static class Consumer implements Runnable {

        private final String bulb;

        public Consumer(String bulb) {
            this.bulb = bulb;
        }

        @Override
        public void run() {

            try {
                if (runningConsumer) {
                    Thread.sleep(rnd.nextInt(MAX_CONS_TIME_MS));
                    LOGGER.info("Packed -" + bulb);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                LOGGER.severe("Exception:" + e);

            }
        }


    }


    public static void startAssemblyLine() {
        if (runningConsumer || runningProducer) {
            LOGGER.info("Assembly line is already running");
            return;
        }
        LOGGER.info("\n\n Starting Assembly line...");

        runningProducer = true;
        producerService = Executors.newSingleThreadExecutor();

        runningConsumer = true;
        consumerService = Executors.newSingleThreadExecutor();

        new Thread(AutomatedAssemblyLine::automatedSystem).start();

    }

    @SuppressWarnings("unchecked")
    private static void automatedSystem() {
        while (runningConsumer && runningProducer) {
            String bulb = "Bulb-" + rnd.nextInt(1000);
            Producer producer = new Producer(bulb);
            Future<String> producerFuture = producerService.submit(producer);
            try {
                String checkedBulb = producerFuture.get(MAX_PROD_TIME_MS + 1000, TimeUnit.MILLISECONDS);
                Consumer consumer = new Consumer(checkedBulb);
                if (runningConsumer) {
                    consumerService.execute(consumer);
                }
            } catch (ExecutionException ex) {
                LOGGER.severe(() -> "Exception: " + ex.getCause());
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                LOGGER.severe(() -> "Exception: " + ex);
            } catch (TimeoutException ex) {
                LOGGER.severe("The producer doesn't respect the checking time!");
            }

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
