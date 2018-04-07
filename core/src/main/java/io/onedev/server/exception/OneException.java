package io.onedev.server.exception;

public class OneException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public OneException(String message) {
		super(message);
	}
	
	public OneException(String message, Throwable cause) {
		super(message, cause);
	}

	public OneException(Throwable cause) {
		super(cause);
	}
	
}
