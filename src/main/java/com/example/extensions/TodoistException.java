package com.example.extensions;

public class TodoistException extends RuntimeException{

    public TodoistException(String message) {
        super(message);
    }

    public TodoistException(String message, Throwable cause) {
        super(message, cause);
    }
}