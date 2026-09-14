package com.titran.pingcortex.exception;

public class UnsupportedProviderException extends RuntimeException{
    public UnsupportedProviderException(String provider) {
        super("Unsupported AI provider: " + provider);
    }
}
