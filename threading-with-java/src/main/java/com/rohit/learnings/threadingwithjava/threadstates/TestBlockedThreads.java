package com.rohit.learnings.threadingwithjava.threadstates;

public class TestBlockedThreads {
    public static void main(String[] args) throws InterruptedException {
        BlockedState blockedState = new BlockedState();
        blockedState.blockedThread();
    }
}
