package com.example.extensions;

public class FileFormatException extends Exception {

	private static final long serialVersionUID = -476544615759737251L;

	public FileFormatException(String message) {
		super(message);
	}
	
	public FileFormatException(String message, Throwable cause) {
		super(message, cause);
	}
}
