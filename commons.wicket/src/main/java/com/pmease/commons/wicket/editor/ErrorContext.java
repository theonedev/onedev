package com.pmease.commons.wicket.editor;

public interface ErrorContext {
	
	void addError(String errorMessage);
	
	boolean hasErrors();
	
	ErrorContext getErrorContext(PathSegment pathSegment);
}
