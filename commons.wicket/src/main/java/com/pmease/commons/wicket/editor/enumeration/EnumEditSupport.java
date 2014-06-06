package com.pmease.commons.wicket.editor.enumeration;

import java.io.Serializable;

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
public class EnumEditSupport implements EditSupport {

	@Override
	public BeanEditContext<? extends Serializable> getBeanEditContext(Class<? extends Serializable> beanClass) {
		return null;
	}

	@Override
	public PropertyEditContext<? extends Serializable> getPropertyEditContext(Class<? extends Serializable> beanClass, String propertyName) {
		PropertyDescriptor propertyDescriptor = new PropertyDescriptorImpl(beanClass, propertyName);
		
        if (Enum.class.isAssignableFrom(propertyDescriptor.getPropertyClass())) {
            return new PropertyEditContext<Enum<?>>(propertyDescriptor) {

				@Override
				public Component renderForView(String componentId, IModel<Enum<?>> model) {
			        if (model.getObject() != null) {
			            return new Label(componentId, model.getObject().toString());
			        } else {
			            return new Label(componentId, "<i>Not Defined</i>").setEscapeModelStrings(false);
			        }
				}

				@Override
				public PropertyEditor<Enum<?>> renderForEdit(String componentId, IModel<Enum<?>> model) {
					return new EnumPropertyEditor(componentId, this, model);
				}
            	
            };
        } else {
            return null;
        }
	}

}
