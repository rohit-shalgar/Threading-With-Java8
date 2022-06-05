package com.rohit.learnings.threadingwithjava.threadstates;

public class BlockedState {

    public void blockedThread() throws InterruptedException {
        Thread t1 = new Thread(new SyncCode());
        Thread t2 = new Thread(new SyncCode());
        System.out.println(Runtime.getRuntime().availableProcessors());
        t1.start();
        Thread.sleep(2000);
        t2.start();
        Thread.sleep(2000);

        System.out.println("BlockedThread t1: "
                + t1.getState() + "(" + t1.getName() + ")");
        System.out.println("BlockedThread t2: "
                + t2.getState() + "(" + t2.getName() + ")");

        System.exit(0);

    }
}
