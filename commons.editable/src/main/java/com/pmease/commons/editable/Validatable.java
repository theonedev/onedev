package com.pmease.commons.editable;

public interface Validatable<T> {

	void validate(EditContext<T> editContext);

}
