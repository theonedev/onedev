package com.pmease.commons.editable.typeconverter;

import com.pmease.commons.util.ExceptionUtils;

@SuppressWarnings("serial")
public class ConversionException extends RuntimeException {
	public ConversionException(String message) {
		super(message);
	}

	public ConversionException(String message, Object...factors) {
		super(ExceptionUtils.buildMessage(message, factors));
	}
}
