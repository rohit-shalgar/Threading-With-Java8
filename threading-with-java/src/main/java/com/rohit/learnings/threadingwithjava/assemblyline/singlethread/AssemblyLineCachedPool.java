package com.rohit.learnings.threadingwithjava.assemblyline.singlethread;

//Producer does not wait for consumer and is able to pack bulbs faster
//As the loaD ON CONSUMER increases,we need to add more consumers using cached pool
// A supervisior will be present to see the proper flow.

import java.util.Queue;
import java.util.Random;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

public final class AssemblyLineCachedPool {

    private static final Logger LOGGER = Logger.getLogger(AssemblyLineCachedPool.class.getName());

    private static final Producer producer = new Producer();
    private static final Consumer consumer = new Consumer();

    private static ExecutorService producerService;
    private static ExecutorService consumerService;
    /*
        TransferQueue is a BlockingQueue in which the producers may wait for the consumers to receive elements. B
        lockingQueue implementations are thread-safe
        The workflow between producer and consumer is of the First In First Out type
        (FIFO: the first bulb checked is the first bulb packed) therefore LinkedTransferQueue can be a good choice.
     */
    private static BlockingQueue<String> queue = new LinkedBlockingQueue<>();


    private AssemblyLineCachedPool() {
        throw new AssertionError("There is a single assembly line.");
    }

    private static final int MAX_PROD_TIME_MS = 1000;
    private static final int MAX_CONS_TIME_MS = 10 * 1000;
    private static final int TIMEOUT_MS = MAX_CONS_TIME_MS + 1000;
    private static final Random rnd = new Random();
    private static int extraProdTime = 0; // need to slow down the producer
    private static volatile boolean runningProducer;

    private static class Producer implements Runnable {

        @Override
        public void run() {
            while (runningProducer) {
                String bulb = "Bulb-" + rnd.nextInt(1000);
                try {
                    Thread.sleep(rnd.nextInt(MAX_PROD_TIME_MS) + extraProdTime);
                    queue.offer(bulb);
                    LOGGER.info("transferred-" + bulb);

                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    LOGGER.severe("Exception:" + e);
                    break;
                }
            }


        }
    }

    private static volatile boolean runningConsumer;
    private static final AtomicInteger nrOfConsumers = new AtomicInteger();
    private static final ThreadGroup consumerGroup = new ThreadGroup("consumers");

    private static class Consumer implements Runnable {

        @Override
        public void run() {
            while (runningConsumer && queue.size() > 0 || nrOfConsumers.get() == 1) {
                try {
                    String bulb = queue.poll(MAX_PROD_TIME_MS + extraProdTime, TimeUnit.MILLISECONDS);
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

            nrOfConsumers.decrementAndGet();
            LOGGER.warning(() -> "### Thread " +
                    Thread.currentThread().getName()
                    + " is going back to the pool in 60 seconds for now!");

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
        consumerService = Executors.newCachedThreadPool(
                (Runnable r) -> new Thread(consumerGroup, r) // number of active threads in a group, this is thread factory implementation.
        );
        nrOfConsumers.incrementAndGet();// active working threads, consumer group will show all the threads, active and non-active both.
        consumerService.execute(consumer);

        monitorQueueSize();
        slowDownProducers();
    }


    //Will check if the Queue size is > 5 and consumers are less than 50, then will call more consumers to pick up.
    //This will be scheduled at every 3 seconds.
    private static final int MAX_NUMBER_OF_CONSUMERS = 50;
    private static final int MAX_QUEUE_SIZE_ALLOWED = 5;
    private static final int MONITOR_QUEUE_INITIAL_DELAY_MS = 5000;
    private static final int MONITOR_QUEUE_RATE_MS = 3000;
    private static ScheduledExecutorService monitorService;

    private static void monitorQueueSize() {

        monitorService = Executors.newSingleThreadScheduledExecutor();
        monitorService.scheduleAtFixedRate(() -> {
            if (queue.size() > MAX_QUEUE_SIZE_ALLOWED && consumerGroup.activeCount() < MAX_NUMBER_OF_CONSUMERS) {
                LOGGER.warning("Starting a new consumer....");
                nrOfConsumers.incrementAndGet();
                consumerService.execute(consumer);
            }
            LOGGER.warning("##Bulbs remaining" + queue.size()
                    + "| Active threads- " + consumerGroup.activeCount() +
                    "|no of consumers -" + nrOfConsumers.get()
                    + "| idle consumers -" + (consumerGroup.activeCount() - nrOfConsumers.get()));
        }, MONITOR_QUEUE_INITIAL_DELAY_MS, MONITOR_QUEUE_RATE_MS, TimeUnit.MILLISECONDS);


    }

    // will slow down producers after a while, as producers are expected to be tired from continuous fast working.
    private static final int SLOW_DOWN_PRODUCER_MS = 20 * 1000;
    private static final int EXTRA_TIME_MS = 4 * 1000;
    private static ScheduledExecutorService slowDownProducerService;

    private static void slowDownProducers() {
        slowDownProducerService = Executors.newSingleThreadScheduledExecutor();
        slowDownProducerService.schedule(() -> {
            LOGGER.warning("Slowing down the producer....");
            extraProdTime = EXTRA_TIME_MS;
        }, SLOW_DOWN_PRODUCER_MS, TimeUnit.MILLISECONDS);
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
