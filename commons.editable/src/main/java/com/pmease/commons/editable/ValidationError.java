package com.pmease.commons.editable;

import java.io.Serializable;

@SuppressWarnings("serial")
public class ValidationError implements Serializable {
	
	private final String propertyPath;
	
	private final String errorMessage;

	public ValidationError(String propertyPath, ValidationError error) {
		if (error.getPropertyPath() != null) {
			if (error.getPropertyPath().startsWith("["))
				this.propertyPath = propertyPath + error.getPropertyPath();
			else
				this.propertyPath = propertyPath + "." + error.getPropertyPath();
		}
		else
			this.propertyPath = propertyPath;
		
		this.errorMessage = error.getErrorMessage();
	}
	
	public ValidationError(String propertyPath, String errorMessage) {
		this.propertyPath = propertyPath;
		this.errorMessage = errorMessage;
	}
	
	public String getPropertyPath() {
		return propertyPath;
	}

	public String getErrorMessage() {
		return errorMessage;
	}

	@Override
	public String toString() {
		if (propertyPath != null)
			return propertyPath + ": " + errorMessage;
		else
			return errorMessage;
	}

}
