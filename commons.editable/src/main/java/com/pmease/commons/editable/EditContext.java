package com.pmease.commons.editable;

import java.io.Serializable;
import java.util.List;

public interface EditContext<T> extends Serializable {
	
	void validate();
	
	List<ValidationError> findValidationErrors();
	
	void renderForEdit(T renderContext);
	
	void renderForView(T renderContext);
}
