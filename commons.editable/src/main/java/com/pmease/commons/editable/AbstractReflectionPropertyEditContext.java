package com.pmease.commons.editable;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.pmease.commons.loader.AppLoader;

@SuppressWarnings("serial")
public abstract class AbstractReflectionPropertyEditContext extends PropertyEditContext {

	private BeanEditContext valueContext;
	
	public AbstractReflectionPropertyEditContext(Serializable bean, String propertyName) {
		super(bean, propertyName);
		
		propertyValueChanged(getPropertyValue());
	}

	public BeanEditContext getValueContext() {
		return valueContext;
	}

	protected void propertyValueChanged(Serializable propertyValue) {
		if (propertyValue != null) {
			EditSupportRegistry registry = AppLoader.getInstance(EditSupportRegistry.class);
			valueContext = registry.getBeanEditContext(propertyValue);
		} else {
			valueContext = null;
		}
	}


	@Override
	public void setPropertyValue(Serializable propertyValue) {
		super.setPropertyValue(propertyValue);
		propertyValueChanged(propertyValue);
	}

	@Override
	protected void doValidation() {
		super.doValidation();
		
		if (valueContext != null)
			valueContext.validate();
	}

	@Override
	public List<ValidationError> findValidationErrors() {
		List<ValidationError> validationErrors = new ArrayList<ValidationError>();
		validationErrors.addAll(getValidationErrors());
		
		if (valueContext != null) {
			validationErrors.addAll(valueContext.findValidationErrors());
		}
		return validationErrors;
	}
	
}
