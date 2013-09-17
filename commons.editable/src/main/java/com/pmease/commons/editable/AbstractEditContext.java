package com.pmease.commons.editable;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

@SuppressWarnings("serial")
public abstract class AbstractEditContext<T> implements EditContext<T> {

	private List<ValidationError> validationErrors = new ArrayList<ValidationError>();
	
	private final Serializable bean;
	
	public AbstractEditContext(Serializable bean) {
		this.bean = bean;
	}
	
	public Serializable getBean() {
		return bean;
	}
	
	public List<ValidationError> getValidationErrors() {
		return validationErrors;
	}

	@Override
	public List<ValidationError> findValidationErrors() {
		return getValidationErrors();
	}

	@Override
	public final void validate() {
		validationErrors.clear();
		doValidation();
	}
	
	protected abstract void doValidation();

	public void error(String errorMessage) {
		validationErrors.add(new ValidationError(null, errorMessage));
	}

	public void error(@Nullable String propertyPath, String errorMessage) {
		validationErrors.add(new ValidationError(propertyPath, errorMessage));
	}
	
}
