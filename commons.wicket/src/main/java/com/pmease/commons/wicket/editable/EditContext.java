package com.pmease.commons.wicket.editable;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import org.apache.wicket.Component;

public interface EditContext extends Serializable {
	
	Serializable getBean();
	
	void updateBean();
	
	void validate();
	
	List<String> getValidationErrors();
	
	void clearValidationErrors();
	
	boolean hasValidationErrors();
	
	Map<Serializable, EditContext> getChildContexts();
	
	EditContext findChildContext(List<Serializable> contextPath, boolean exactMatch);
	
	void addValidationError(String errorMessage);
	
	Component renderForEdit(String componentId);
	
	Component renderForView(String componentId);

}
