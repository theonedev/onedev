package com.pmease.commons.git;

public class GitException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public GitException() {
	}
	
	public GitException(String message) {
		super(message);
	}
	
	public GitException(String message, Throwable cause) {
		super(message, cause);
	}

	public GitException(Throwable cause) {
		super(cause);
	}
	
}

