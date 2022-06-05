package com.rohit.learnings.threadingwithjava.assemblyline;

public class TestAssemblyLineMultiplePC {

    public static void main(String[] args) throws InterruptedException {

        System.setProperty("java.util.logging.SimpleFormatter.format",
                "[%1$tT] [%4$-7s] %5$s %n");

        AssemblyLineMultiplePC.startAssemblyLine();
        Thread.sleep(10 * 1000);
        AssemblyLineMultiplePC.shutDownAssemblyLine();

        System.out.println("Starting the assembly line again...");
        AssemblyLineMultiplePC.startAssemblyLine();
        Thread.sleep(10 * 1000);
        AssemblyLineMultiplePC.shutDownAssemblyLine();
    }
}
