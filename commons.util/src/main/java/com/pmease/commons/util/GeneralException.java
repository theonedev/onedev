package com.pmease.commons.util;

@SuppressWarnings("serial")
public class GeneralException extends RuntimeException {

	public GeneralException(String message) {
		super(message);
	}
	
	public GeneralException(String message, Throwable cause) {
		super(message, cause);
	}

}
