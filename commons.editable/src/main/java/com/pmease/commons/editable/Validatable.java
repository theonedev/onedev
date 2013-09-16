package com.pmease.commons.editable;

import java.util.Set;

public interface Validatable {

	void validate(Set<String> propertyNames, ErrorContext errorContext);

}
