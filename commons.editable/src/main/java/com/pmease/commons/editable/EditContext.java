package com.pmease.commons.editable;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

public interface EditContext<T> extends Serializable {
	
	void validate();
	
	List<ValidationError> getValidationErrors(boolean recursive);
	
	Map<Serializable, EditContext<T>> getChildContexts();
	
	EditContext<T> getChildContext(Serializable propertyName);
	
	boolean hasError(Serializable propertyName, boolean recursive);
	
	boolean hasError(boolean recursive);
	
	void error(String errorMessage);
	
	void renderForEdit(T renderContext);
	
	void renderForView(T renderContext);
	
}
