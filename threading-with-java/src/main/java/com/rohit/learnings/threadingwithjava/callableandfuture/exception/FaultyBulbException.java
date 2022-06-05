package com.rohit.learnings.threadingwithjava.callableandfuture.exception;

import java.util.concurrent.ExecutionException;

public class FaultyBulbException extends ExecutionException {
    private static final long serialVersionUID = 1L;

    public FaultyBulbException() {
        super();
    }

    public FaultyBulbException(String message) {
        super(message);
    }
}
