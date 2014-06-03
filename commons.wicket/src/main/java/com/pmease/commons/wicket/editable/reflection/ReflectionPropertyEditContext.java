package com.pmease.commons.wicket.editable.reflection;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.apache.wicket.Component;
import org.apache.wicket.markup.html.basic.Label;

import com.pmease.commons.loader.AppLoader;
import com.pmease.commons.wicket.editable.BeanEditContext;
import com.pmease.commons.wicket.editable.EditContext;
import com.pmease.commons.wicket.editable.EditSupportRegistry;
import com.pmease.commons.wicket.editable.PropertyEditContext;

@SuppressWarnings("serial")
public class ReflectionPropertyEditContext extends PropertyEditContext {

	private BeanEditContext valueContext;
	
	public ReflectionPropertyEditContext(Serializable bean, String propertyName) {
		super(bean, propertyName);
		
		propertyValueChanged(getPropertyValue());
	}

	public BeanEditContext getValueContext() {
		return valueContext;
	}

	private void propertyValueChanged(Serializable propertyValue) {
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

	@Override
	public Component renderForEdit(String componentId) {
		return new ReflectionPropertyEditor(componentId, this);
	}

	@Override
	public Component renderForView(String componentId) {
		EditContext valueContext = getValueContext();
		if (valueContext != null) {
			return valueContext.renderForView(componentId);
		} else {
			return new Label(componentId, "<i>Not Defined</i>").setEscapeModelStrings(false);
		}
	}

}
