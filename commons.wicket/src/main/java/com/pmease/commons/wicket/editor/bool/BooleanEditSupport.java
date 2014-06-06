package com.pmease.commons.wicket.editor.bool;

import java.io.Serializable;
import java.lang.reflect.Method;

import org.apache.wicket.Component;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.IModel;

import com.pmease.commons.editable.PropertyDescriptor;
import com.pmease.commons.editable.PropertyDescriptorImpl;
import com.pmease.commons.wicket.editor.BeanEditContext;
import com.pmease.commons.wicket.editor.EditSupport;
import com.pmease.commons.wicket.editor.PropertyEditContext;
import com.pmease.commons.wicket.editor.PropertyEditor;

@SuppressWarnings("serial")
public class BooleanEditSupport implements EditSupport {

	@Override
	public BeanEditContext<? extends Serializable> getBeanEditContext(Class<? extends Serializable> beanClass) {
		return null;
	}

	@Override
	public PropertyEditContext<? extends Serializable> getPropertyEditContext(Class<? extends Serializable> beanClass, String propertyName) {
		PropertyDescriptor propertyDescriptor = new PropertyDescriptorImpl(beanClass, propertyName);
		
		Method propertyGetter = propertyDescriptor.getPropertyGetter();
		
		Class<?> propertyClass = propertyGetter.getReturnType();
		if (propertyClass == boolean.class || propertyClass == Boolean.class) {
			return new PropertyEditContext<Boolean>(propertyDescriptor) {

				@Override
				public Component renderForView(String componentId, IModel<Boolean> model) {
					if (model.getObject() != null) {
						return new Label(componentId, model.getObject().toString());
					} else {
						return new Label(componentId, "<i>Not Defined</i>").setEscapeModelStrings(false);
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
