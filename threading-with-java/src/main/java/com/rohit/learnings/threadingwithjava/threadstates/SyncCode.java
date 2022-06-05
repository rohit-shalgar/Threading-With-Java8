package com.rohit.learnings.threadingwithjava.threadstates;

public class SyncCode implements Runnable {
    @Override
    public void run() {
        System.out.println("Thread being run is -"+Thread.currentThread().getName());
        syncThread();
    }

    private static synchronized  void syncThread() {
        System.out.println("The thread -"+ Thread.currentThread().getName()+"is in synchronised method.");
        while (true){
            //doNothing - just an infinite loop.
        }
    }
}
