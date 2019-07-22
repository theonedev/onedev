package io.onedev.server.web.editable;

public interface ErrorContext {
	
	void addError(String errorMessage);
	
	boolean hasErrors(boolean recursive);
	
	/**
	 * Get error context of specified element
	 * @param element
	 * @return
	 * 			error context of specified path, <tt>null</tt> if error of the element should be ignored 
	 * 			(for instance when visibility of the property depends on another property, or is excluded etc.)
	 */
	ErrorContext getErrorContext(PathElement element);
}
