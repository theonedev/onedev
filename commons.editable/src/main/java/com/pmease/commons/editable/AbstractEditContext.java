package com.pmease.commons.editable;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.pmease.commons.util.GeneralException;

@SuppressWarnings("serial")
public abstract class AbstractEditContext<T> implements EditContext<T> {

	protected List<String> validationErrorMessages = new ArrayList<String>();
	
	private final Serializable bean;
	
	public AbstractEditContext(Serializable bean) {
		this.bean = bean;
	}
	
	public Serializable getBean() {
		return bean;
	}
	
	@Override
	public List<ValidationError> getValidationErrors(boolean recursive) {
		List<ValidationError> validationErrors = new ArrayList<ValidationError>();
		for (String each: validationErrorMessages) {
			validationErrors.add(new ValidationError(each));
		}
		
		if (recursive && getChildContexts() != null) {
			for (Map.Entry<Serializable, EditContext<T>> eachEntry: getChildContexts().entrySet()) {
				for (ValidationError eachError: eachEntry.getValue().getValidationErrors(true)) {
					PropertyPath newPath = eachError.getPropertyPath().prepend(eachEntry.getKey());
					validationErrors.add(new ValidationError(newPath, eachError.getErrorMessage()));
				}
			}
		}
		return Collections.unmodifiableList(validationErrors);
	}
	
	@Override
	public void validate() {
		validationErrorMessages.clear();
		if (getChildContexts() != null) {
			for (EditContext<T> each: getChildContexts().values())
				each.validate();
		}
		
		doValidation();
	}
	
	protected abstract void doValidation();

	@Override
	public void error(String errorMessage) {
		validationErrorMessages.add(errorMessage);
	}

	@Override
	public EditContext<T> getChildContext(Serializable propertyName) {
		if (getChildContexts() != null) {
			EditContext<T> childContext = getChildContexts().get(propertyName);
			if (childContext != null)
				return childContext;
		}

		throw new GeneralException("Unable to find child context of property '%s'", propertyName);
	}

	@Override
	public boolean hasError(Serializable propertyName, boolean recursive) {
		EditContext<T> childContext = getChildContext(propertyName);
		return !childContext.getValidationErrors(recursive).isEmpty();
	}

	@Override
	public boolean hasError(boolean recursive) {
		return !getValidationErrors(recursive).isEmpty();
	}

}
