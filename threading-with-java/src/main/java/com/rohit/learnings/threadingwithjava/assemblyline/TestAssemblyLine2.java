package com.rohit.learnings.threadingwithjava.assemblyline;

public class TestAssemblyLine2 {

    public static void main(String[] args) throws InterruptedException {

        System.setProperty("java.util.logging.SimpleFormatter.format",
                "[%1$tT] [%4$-7s] %5$s %n");

        AssemblyLine2.startAssemblyLine();
        Thread.sleep(10 * 1000);
        AssemblyLine2.shutDownAssemblyLine();

        System.out.println("Starting the assembly line again...");
        AssemblyLine2.startAssemblyLine();
        Thread.sleep(10 * 1000);
        AssemblyLine2.shutDownAssemblyLine();
    }
}
