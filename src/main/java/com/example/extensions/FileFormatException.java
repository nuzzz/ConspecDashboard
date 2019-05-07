package com.example.extensions;

public class FileFormatException extends Exception {
	public FileFormatException(String message) {
		super(message);
	}
	
	public FileFormatException(String message, Throwable cause) {
		super(message, cause);
	}
}
