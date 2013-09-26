package com.pmease.commons.editable;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

public interface EditContext extends Serializable {
	
	Serializable getBean();
	
	void validate();
	
	List<ValidationError> getValidationErrors(boolean recursive);
	
	Map<Serializable, EditContext> getChildContexts();
	
	EditContext getChildContext(Serializable propertyName);
	
	boolean hasValidationError(Serializable propertyName, boolean recursive);
	
	boolean hasValidationError(boolean recursive);
	
	void error(String errorMessage);
	
	Object renderForEdit(Object renderParam);
	
	Object renderForView(Object renderParam);
	
}
