package com.titran.pingcortex.exception;

public class ConceptNotFoundException extends RuntimeException {
    public ConceptNotFoundException() {
        super("Concept not found");
    }
}
