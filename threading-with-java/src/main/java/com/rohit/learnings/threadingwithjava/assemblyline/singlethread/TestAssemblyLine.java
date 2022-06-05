package com.rohit.learnings.threadingwithjava.assemblyline.singlethread;

public class TestAssemblyLine {

    public static void main(String[] args) throws InterruptedException {

        System.setProperty("java.util.logging.SimpleFormatter.format",
                "[%1$tT] [%4$-7s] %5$s %n");

        AssemblyLine.startAssemblyLine();
        Thread.sleep(10 * 1000);
        AssemblyLine.shutDownAssemblyLine();

        System.out.println("Starting the assembly line again...");
        AssemblyLine.startAssemblyLine();
        Thread.sleep(10 * 1000);
        AssemblyLine.shutDownAssemblyLine();
    }
}
