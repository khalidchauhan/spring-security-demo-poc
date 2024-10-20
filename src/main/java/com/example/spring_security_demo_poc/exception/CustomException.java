package com.example.spring_security_demo_poc.exception;

public class CustomException extends RuntimeException {
    public CustomException(String message) {
        super(message);
    }
}
