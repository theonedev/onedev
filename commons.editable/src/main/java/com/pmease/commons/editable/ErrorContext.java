package com.pmease.commons.editable;

public interface ErrorContext {
	void error(String propertyPath, String errorMessage);
	
	void error(String errorMessage);
}
