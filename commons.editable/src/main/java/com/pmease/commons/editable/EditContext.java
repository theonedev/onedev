package com.pmease.commons.editable;

import java.io.Serializable;
import java.util.List;

public interface EditContext extends Serializable {
	
	void validate();
	
	List<ValidationError> findValidationErrors();
	
}
