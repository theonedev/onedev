package com.pmease.commons.util;

public class DependencyException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	DependencyException(String message) {
		super(message);
	}

	DependencyException(String message, Throwable cause) {
		super(message, cause);
	}

}
