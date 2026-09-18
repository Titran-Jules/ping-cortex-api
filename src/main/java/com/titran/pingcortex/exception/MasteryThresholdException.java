package com.titran.pingcortex.exception;

public class MasteryThresholdException extends RuntimeException {
    public MasteryThresholdException() {
        super("Mastery threshold not yet reached");
    }
}
