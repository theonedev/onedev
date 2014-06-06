package com.pmease.commons.wicket.editor.polymorphic;

import java.io.Serializable;

import org.apache.wicket.Component;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.IModel;

import com.pmease.commons.editable.PropertyDescriptor;
import com.pmease.commons.editable.PropertyDescriptorImpl;
import com.pmease.commons.editable.annotation.Editable;
import com.pmease.commons.util.ClassUtils;
import com.pmease.commons.wicket.editor.BeanEditContext;
import com.pmease.commons.wicket.editor.EditSupport;
import com.pmease.commons.wicket.editor.PropertyEditContext;
import com.pmease.commons.wicket.editor.PropertyEditor;

@SuppressWarnings("serial")
public class PolymorphicEditSuport implements EditSupport {

	@Override
	public BeanEditContext<? extends Serializable> getBeanEditContext(Class<? extends Serializable> beanClass) {
		return null;
	}

	@Override
	public PropertyEditContext<? extends Serializable> getPropertyEditContext(Class<? extends Serializable> beanClass, String propertyName) {
		PropertyDescriptor propertyDescriptpr = new PropertyDescriptorImpl(beanClass, propertyName);
		Class<? extends Serializable> propertyClass = propertyDescriptpr.getPropertyClass();
		if (propertyClass.getAnnotation(Editable.class) != null && !ClassUtils.isConcrete(propertyClass)) {
			return new PropertyEditContext<Serializable>(propertyDescriptpr) {

				@Override
				public Component renderForView(String componentId, IModel<Serializable> model) {
					if (model.getObject() != null)
						return new PolymorphicPropertyViewer(componentId, this, model.getObject());
					else
						return new Label(componentId, "<i>Not Defined</i>").setEscapeModelStrings(false);
				}

				@Override
				public PropertyEditor<Serializable> renderForEdit(String componentId, IModel<Serializable> model) {
					return new PolymorphicPropertyEditor(componentId, this, model);
				}
				
			};
		} else {
			return null;
		}
	}

}
