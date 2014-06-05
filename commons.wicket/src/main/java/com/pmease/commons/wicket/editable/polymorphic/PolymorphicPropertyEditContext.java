package com.pmease.commons.wicket.editable.polymorphic;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.wicket.Component;
import org.apache.wicket.markup.html.basic.Label;

import com.google.common.base.Preconditions;
import com.pmease.commons.editable.EditableUtils;
import com.pmease.commons.loader.AppLoader;
import com.pmease.commons.loader.ImplementationRegistry;
import com.pmease.commons.wicket.editable.BeanEditContext;
import com.pmease.commons.wicket.editable.EditContext;
import com.pmease.commons.wicket.editable.EditSupportRegistry;
import com.pmease.commons.wicket.editable.PropertyEditContext;

@SuppressWarnings("serial")
public class PolymorphicPropertyEditContext extends PropertyEditContext {

	private final List<Class<?>> implementations = new ArrayList<Class<?>>();
	
	private BeanEditContext valueContext;

	public PolymorphicPropertyEditContext(Serializable bean, String propertyName) {
		super(bean, propertyName);

		Class<?> elementBaseClass = getPropertyGetter().getReturnType();
		ImplementationRegistry registry = AppLoader.getInstance(ImplementationRegistry.class);
		implementations.addAll(registry.getImplementations(elementBaseClass));
		
		Preconditions.checkArgument(
				!implementations.isEmpty(), 
				"Can not find implementations for '" + elementBaseClass + "'.");
		
		EditableUtils.sortAnnotatedElements(implementations);
		
		propertyValueChanged(getPropertyValue());
	}

	public List<Class<?>> getImplementations() {
		return implementations;
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

	public BeanEditContext getValueContext() {
		return valueContext;
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
		return new PolymorphicPropertyEditor(componentId, this);
	}

	@Override
	public Component renderForView(String componentId) {
		EditContext valueContext = getValueContext();
		
		if (valueContext != null)
			return valueContext.renderForView(componentId);
		else
			return new Label(componentId, "<i>Not Defined</i>").setEscapeModelStrings(false);
	}

}
