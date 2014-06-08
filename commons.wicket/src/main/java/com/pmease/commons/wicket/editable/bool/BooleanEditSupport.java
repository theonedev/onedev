package com.pmease.commons.wicket.editable.bool;

import java.lang.reflect.Method;

import org.apache.wicket.Component;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.IModel;

import com.pmease.commons.editable.PropertyDescriptor;
import com.pmease.commons.editable.PropertyDescriptorImpl;
import com.pmease.commons.wicket.editable.BeanContext;
import com.pmease.commons.wicket.editable.EditSupport;
import com.pmease.commons.wicket.editable.NotDefinedLabel;
import com.pmease.commons.wicket.editable.PropertyContext;
import com.pmease.commons.wicket.editable.PropertyEditor;

@SuppressWarnings("serial")
public class BooleanEditSupport implements EditSupport {

	@Override
	public BeanContext<?> getBeanEditContext(Class<?> beanClass) {
		return null;
	}

	@Override
	public PropertyContext<?> getPropertyEditContext(Class<?> beanClass, String propertyName) {
		PropertyDescriptor propertyDescriptor = new PropertyDescriptorImpl(beanClass, propertyName);
		
		Method propertyGetter = propertyDescriptor.getPropertyGetter();
		
		Class<?> propertyClass = propertyGetter.getReturnType();
		if (propertyClass == boolean.class || propertyClass == Boolean.class) {
			return new PropertyContext<Boolean>(propertyDescriptor) {

				@Override
				public Component renderForView(String componentId, IModel<Boolean> model) {
					if (model.getObject() != null) {
						return new Label(componentId, model.getObject().toString());
					} else {
						return new NotDefinedLabel(componentId);
					}
				}

				@Override
				public PropertyEditor<Boolean> renderForEdit(String componentId, IModel<Boolean> model) {
					if (isPropertyRequired())
						return new BooleanPropertyEditor(componentId, this, model);
					else
						return new NullableBooleanPropertyEditor(componentId, this, model);
				}
				
			};
		} else {
			return null;
		}
	}

}
