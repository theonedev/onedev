package com.pmease.commons.wicket.editable;

public interface ErrorContext {
	
	void addError(String errorMessage);
	
	boolean hasErrors();
	
	ErrorContext getErrorContext(PathSegment pathSegment);
}
