package com.pmease.commons.editable;

import com.pmease.commons.util.GeneralException;

@SuppressWarnings("serial")
public class ValidationException extends GeneralException {

	public ValidationException(String format, Object... args) {
		super(format, args);
	}
	
}
