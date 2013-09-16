package com.pmease.commons.util;

@SuppressWarnings("serial")
public class GeneralException extends RuntimeException {

	public GeneralException(String format, Object...args) {
		super(String.format(format, args));
	}
	
}
