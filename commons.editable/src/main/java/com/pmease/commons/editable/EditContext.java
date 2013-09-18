package com.pmease.commons.editable;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

public interface EditContext extends Serializable {
	
	void validate();
	
	List<ValidationError> getValidationErrors(boolean recursive);
	
	Map<Serializable, EditContext> getChildContexts();
	
	EditContext getChildContext(Serializable propertyName);
	
	boolean hasError(Serializable propertyName, boolean recursive);
	
	boolean hasError(boolean recursive);
	
	void error(String errorMessage);
	
	Object renderForEdit(Object renderParam);
	
	Object renderForView(Object renderParam);
	
}
