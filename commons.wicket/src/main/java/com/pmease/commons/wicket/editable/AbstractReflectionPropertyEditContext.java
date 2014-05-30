package com.pmease.commons.wicket.editable;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

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
	public Map<Serializable, EditContext> getChildContexts() {
		if (valueContext != null)
			return valueContext.getChildContexts();
		else
			return new HashMap<>();
	}

}
