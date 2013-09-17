package com.pmease.commons.editable;

import java.io.Serializable;

@SuppressWarnings("serial")
public class ValidationError implements Serializable {
	
	private final PropertyPath propertyPath;
	
	private final String errorMessage;

	public ValidationError(PropertyPath propertyPath, String errorMessage) {
		this.propertyPath = propertyPath;
		this.errorMessage = errorMessage;
	}
	
	public ValidationError(String errorMessage) {
		this(new PropertyPath(), errorMessage);
	}
	
	public PropertyPath getPropertyPath() {
		return propertyPath;
	}

	public String getErrorMessage() {
		return errorMessage;
	}

	@Override
	public String toString() {
		if (!propertyPath.getElements().isEmpty())
			return propertyPath + ": " + errorMessage;
		else
			return errorMessage;
	}

}
