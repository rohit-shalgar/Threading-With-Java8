package com.rohit.learnings.threadingwithjava.callableandfuture;

public class TestAutomatedAssemblyLine {

    public static void main(String[] args) throws InterruptedException {

        System.setProperty("java.util.logging.SimpleFormatter.format",
                "[%1$tT] [%4$-7s] %5$s %n");

        AutomatedAssemblyLine.startAssemblyLine();
        Thread.sleep(30 * 1000);
        AutomatedAssemblyLine.shutDownAssemblyLine();
    }
}
