package com.rohit.learnings.threadingwithjava.executor;

public class SimpleThreadExecutor implements Runnable {

    private final int taskId;

    public SimpleThreadExecutor(int taskId) {
        this.taskId = taskId;
    }

    public final int getTaskId(){
        return this.taskId;
    }

    @Override
    public void run() {
        try {
            Thread.sleep(1000);
            System.out.println("Task is scheduled with task id -" + this.taskId + "by the thread - " +
                                Thread.currentThread().getName());
        } catch (InterruptedException e) {
            System.out.println(e.getMessage());
        }

    }
}
