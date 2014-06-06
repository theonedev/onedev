package com.pmease.commons.wicket.editor.reflection;

import java.io.Serializable;

import org.apache.wicket.Component;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.IModel;

import com.pmease.commons.editable.PropertyDescriptor;
import com.pmease.commons.editable.PropertyDescriptorImpl;
import com.pmease.commons.editable.annotation.Editable;
import com.pmease.commons.util.ClassUtils;
import com.pmease.commons.wicket.editor.BeanEditContext;
import com.pmease.commons.wicket.editor.BeanEditor;
import com.pmease.commons.wicket.editor.EditSupport;
import com.pmease.commons.wicket.editor.PropertyEditContext;
import com.pmease.commons.wicket.editor.PropertyEditor;

@SuppressWarnings("serial")
public class ReflectionEditSupport implements EditSupport {

	@Override
	public BeanEditContext<? extends Serializable> getBeanEditContext(Class<? extends Serializable> beanClass) {
		if (beanClass.getAnnotation(Editable.class) != null && ClassUtils.isConcrete(beanClass)) {
			return new BeanEditContext<Serializable>(beanClass) {

				@Override
				public Component renderForView(String componentId, IModel<Serializable> model) {
					return new ReflectionBeanViewer(componentId, this, model);
				}

				@Override
				public BeanEditor<Serializable> renderForEdit(String componentId, IModel<Serializable> model) {
					return new ReflectionBeanEditor(componentId, this, model);
				}
				
			};
		} else {
			return null;
		}
	}

	@Override
	public PropertyEditContext<? extends Serializable> getPropertyEditContext(Class<? extends Serializable> beanClass, String propertyName) {
		PropertyDescriptor propertyDescriptor = new PropertyDescriptorImpl(beanClass, propertyName);
		Class<?> propertyClass = propertyDescriptor.getPropertyClass();
		if (propertyClass.getAnnotation(Editable.class) != null && ClassUtils.isConcrete(propertyClass)) {
			return new PropertyEditContext<Serializable>(propertyDescriptor) {

				@Override
				public Component renderForView(String componentId, IModel<Serializable> model) {
					if (model.getObject() != null) {
						return BeanEditContext.view(componentId, model.getObject());
					} else {
						return new Label(componentId, "<i>Not Defined</i>").setEscapeModelStrings(false);
					}
				}

				@Override
				public PropertyEditor<Serializable> renderForEdit(String componentId, IModel<Serializable> model) {
					return new ReflectionPropertyEditor(componentId, this, model);
				}
				
			};
		} else {
			return null;
		}
	}

}
