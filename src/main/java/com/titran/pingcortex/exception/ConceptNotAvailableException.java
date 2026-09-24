package com.titran.pingcortex.exception;

public class ConceptNotAvailableException extends RuntimeException {
    public ConceptNotAvailableException() {
        super("Concept not available");
    }
}
