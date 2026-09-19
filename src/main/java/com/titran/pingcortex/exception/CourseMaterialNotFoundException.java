package com.titran.pingcortex.exception;

public class CourseMaterialNotFoundException extends RuntimeException {
    public CourseMaterialNotFoundException() {
        super("Course material not found");
    }
}
