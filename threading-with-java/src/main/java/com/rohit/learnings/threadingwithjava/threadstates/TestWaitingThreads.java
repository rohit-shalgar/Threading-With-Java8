package com.rohit.learnings.threadingwithjava.threadstates;

public class TestWaitingThreads {
    public static void main(String[] args) throws InterruptedException {
        WaitingThread waitingThread = new WaitingThread();
        waitingThread.waitingThread();
    }
}
