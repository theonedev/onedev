package com.pmease.commons.wicket.editable.enumeration;

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
public class EnumEditSupport implements EditSupport {

	@Override
	public BeanContext<?> getBeanEditContext(Class<?> beanClass) {
		return null;
	}

	@Override
	public PropertyContext<?> getPropertyEditContext(Class<?> beanClass, String propertyName) {
		PropertyDescriptor propertyDescriptor = new PropertyDescriptorImpl(beanClass, propertyName);
		
        if (Enum.class.isAssignableFrom(propertyDescriptor.getPropertyClass())) {
            return new PropertyContext<Enum<?>>(propertyDescriptor) {

				@Override
				public Component renderForView(String componentId, IModel<Enum<?>> model) {
			        if (model.getObject() != null) {
			            return new Label(componentId, model.getObject().toString());
			        } else {
						return new NotDefinedLabel(componentId);
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
