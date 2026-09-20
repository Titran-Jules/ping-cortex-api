package com.titran.pingcortex.exception;

public class InvalidOptionException extends RuntimeException {
    public InvalidOptionException() {
        super("Invalid selected option for this question");
    }
}
