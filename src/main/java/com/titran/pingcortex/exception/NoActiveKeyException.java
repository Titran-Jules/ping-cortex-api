package com.titran.pingcortex.exception;

public class NoActiveKeyException extends RuntimeException {
    public NoActiveKeyException() {
        super("No active API key configured for this user");
    }
}
