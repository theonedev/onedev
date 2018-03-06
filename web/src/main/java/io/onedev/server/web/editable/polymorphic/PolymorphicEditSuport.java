package io.onedev.server.web.editable.polymorphic;

import java.io.Serializable;
import java.util.Set;

import org.apache.wicket.Component;
import org.apache.wicket.model.IModel;

import io.onedev.server.util.editable.annotation.Editable;
import io.onedev.server.web.editable.BeanContext;
import io.onedev.server.web.editable.EditSupport;
import io.onedev.server.web.editable.NotDefinedLabel;
import io.onedev.server.web.editable.PropertyContext;
import io.onedev.server.web.editable.PropertyDescriptor;
import io.onedev.server.web.editable.PropertyEditor;
import io.onedev.server.web.editable.PropertyViewer;
import io.onedev.utils.ClassUtils;

@SuppressWarnings("serial")
public class PolymorphicEditSuport implements EditSupport {

	@Override
	public BeanContext<?> getBeanEditContext(Class<?> beanClass, Set<String> excludeProperties) {
		return null;
	}

	@Override
	public PropertyContext<?> getPropertyEditContext(Class<?> beanClass, String propertyName) {
		PropertyDescriptor propertyDescriptpr = new PropertyDescriptor(beanClass, propertyName);
		Class<?> propertyClass = propertyDescriptpr.getPropertyClass();
		if (propertyClass.getAnnotation(Editable.class) != null && !ClassUtils.isConcrete(propertyClass)) {
			return new PropertyContext<Serializable>(propertyDescriptpr) {

				@Override
				public PropertyViewer renderForView(String componentId, final IModel<Serializable> model) {
					return new PropertyViewer(componentId, this) {

						@Override
						protected Component newContent(String id, PropertyDescriptor propertyDescriptor) {
							if (model.getObject() != null)
								return new PolymorphicPropertyViewer(id, propertyDescriptor, model.getObject());
							else
								return new NotDefinedLabel(id);
						}
						
					};
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
