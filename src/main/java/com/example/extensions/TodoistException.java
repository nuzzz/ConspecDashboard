package com.example.extensions;

public class TodoistException extends RuntimeException{
	private static final long serialVersionUID = 8277897475869178604L;

	public TodoistException(String message) {
        super(message);
    }

    public TodoistException(String message, Throwable cause) {
        super(message, cause);
    }
}