package com.titran.pingcortex.exception;

public class QuizNotFoundException extends RuntimeException {
    public QuizNotFoundException() {
        super("Quiz not found");
    }
}
