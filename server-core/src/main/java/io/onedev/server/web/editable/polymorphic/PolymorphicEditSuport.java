package io.onedev.server.web.editable.polymorphic;

import java.io.Serializable;

import org.apache.wicket.Component;
import org.apache.wicket.model.IModel;

import io.onedev.commons.utils.ClassUtils;
import io.onedev.server.web.editable.EditSupport;
import io.onedev.server.web.editable.EmptyValueLabel;
import io.onedev.server.web.editable.PropertyContext;
import io.onedev.server.web.editable.PropertyDescriptor;
import io.onedev.server.web.editable.PropertyEditor;
import io.onedev.server.web.editable.PropertyViewer;
import io.onedev.server.web.editable.annotation.Editable;

@SuppressWarnings("serial")
public class PolymorphicEditSuport implements EditSupport {

	@Override
	public PropertyContext<?> getEditContext(PropertyDescriptor descriptor) {
		Class<?> propertyClass = descriptor.getPropertyClass();
		if (propertyClass.getAnnotation(Editable.class) != null && !ClassUtils.isConcrete(propertyClass)) {
			return new PropertyContext<Serializable>(descriptor) {

				@Override
				public PropertyViewer renderForView(String componentId, final IModel<Serializable> model) {
					return new PropertyViewer(componentId, descriptor) {

						@Override
						protected Component newContent(String id, PropertyDescriptor propertyDescriptor) {
							if (model.getObject() != null) {
								return new PolymorphicPropertyViewer(id, propertyDescriptor, model.getObject());
							} else {
								return new EmptyValueLabel(id, propertyDescriptor.getPropertyGetter());
							}
						}
						
					};
				}

				@Override
				public PropertyEditor<Serializable> renderForEdit(String componentId, IModel<Serializable> model) {
					return new PolymorphicPropertyEditor(componentId, descriptor, model);
				}
				
			};
		} else {
			return null;
		}
	}

	@Override
	public int getPriority() {
		return DEFAULT_PRIORITY;
	}
	
}
