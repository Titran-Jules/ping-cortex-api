package com.titran.pingcortex.exception;

public class ApiKeyNotFoundException extends RuntimeException {
    public ApiKeyNotFoundException() {
        super("ApiKey not found");
    }
}
