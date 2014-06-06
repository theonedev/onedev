package com.pmease.commons.wicket.editor.string;

import java.io.Serializable;

import org.apache.wicket.Component;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.IModel;

import com.pmease.commons.editable.PropertyDescriptor;
import com.pmease.commons.editable.PropertyDescriptorImpl;
import com.pmease.commons.editable.annotation.Password;
import com.pmease.commons.wicket.editor.BeanEditContext;
import com.pmease.commons.wicket.editor.EditSupport;
import com.pmease.commons.wicket.editor.PropertyEditContext;
import com.pmease.commons.wicket.editor.PropertyEditor;

@SuppressWarnings("serial")
public class StringEditSupport implements EditSupport {

	@Override
	public BeanEditContext<? extends Serializable> getBeanEditContext(Class<? extends Serializable> beanClass) {
		return null;
	}

	@Override
	public PropertyEditContext<? extends Serializable> getPropertyEditContext(Class<? extends Serializable> beanClass, String propertyName) {
		PropertyDescriptor propertyDescriptor = new PropertyDescriptorImpl(beanClass, propertyName);

		Class<?> propertyClass = propertyDescriptor.getPropertyGetter().getReturnType();
		if (propertyClass == String.class && propertyDescriptor.getPropertyGetter().getAnnotation(Password.class) == null) {
			return new PropertyEditContext<String>(propertyDescriptor) {

				@Override
				public Component renderForView(String componentId, IModel<String> model) {
					if (model.getObject() != null) {
						return new Label(componentId, model.getObject());
					} else {
						return new Label(componentId, "<i>Not Defined</i>").setEscapeModelStrings(false);
					}
				}

				@Override
				public PropertyEditor<String> renderForEdit(String componentId, IModel<String> model) {
					return new StringPropertyEditor(componentId, this, model);
				}
				
			};
		} else {
			return null;
		}
		
	}

}
