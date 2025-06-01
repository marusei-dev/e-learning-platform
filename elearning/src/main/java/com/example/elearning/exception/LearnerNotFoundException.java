package com.example.elearning.exception;

public class LearnerNotFoundException extends RuntimeException {
    public LearnerNotFoundException(String message) {
        super(message);
    }
}
