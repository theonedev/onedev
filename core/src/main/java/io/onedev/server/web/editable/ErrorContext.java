package io.onedev.server.web.editable;

public interface ErrorContext {
	
	void addError(String errorMessage);
	
	boolean hasErrors(boolean recursive);
	
	ErrorContext getErrorContext(PathSegment pathSegment);
}
