package com.pmease.commons.editable;

import java.io.Serializable;
import java.util.Map;

import com.pmease.commons.loader.AppLoader;

@SuppressWarnings("serial")
public abstract class AbstractReflectionPropertyEditContext<T> extends PropertyEditContext<T> {

	private BeanEditContext<T> valueContext;
	
	public AbstractReflectionPropertyEditContext(Serializable bean, String propertyName) {
		super(bean, propertyName);
		
		propertyValueChanged(getPropertyValue());
	}

	public BeanEditContext<T> getValueContext() {
		return valueContext;
	}

	@SuppressWarnings("unchecked")
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
	public Map<Serializable, EditContext<T>> getChildContexts() {
		if (valueContext != null)
			return valueContext.getChildContexts();
		else
			return null;
	}

	@SuppressWarnings("unchecked")
	@Override
	protected void doValidation() {
		super.doValidation();

		// redirect error message of bean level of the property value to be directly 
		// under this property
		if (valueContext != null && valueContext.getBean() instanceof Validatable) {
			((Validatable<T>)valueContext.getBean()).validate(this);
		}
	}
	
}
