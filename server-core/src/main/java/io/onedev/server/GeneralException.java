package io.onedev.server;

public class GeneralException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public GeneralException(String message) {
		super(message);
	}
	
	public GeneralException(String message, Throwable cause) {
		super(message, cause);
	}

	public GeneralException(Throwable cause) {
		super(cause);
	}
	
}
