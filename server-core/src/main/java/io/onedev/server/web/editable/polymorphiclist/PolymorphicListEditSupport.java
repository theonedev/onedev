package io.onedev.server.web.editable.polymorphiclist;

import io.onedev.commons.utils.ClassUtils;
import io.onedev.server.annotation.Editable;
import io.onedev.server.util.ReflectionUtils;
import io.onedev.server.web.editable.*;
import org.apache.wicket.Component;
import org.apache.wicket.model.IModel;

import java.io.Serializable;
import java.lang.reflect.AnnotatedElement;
import java.util.List;

public class PolymorphicListEditSupport implements EditSupport {

	@Override
	public PropertyContext<?> getEditContext(PropertyDescriptor descriptor) {
		if (List.class.isAssignableFrom(descriptor.getPropertyClass())) {
			Class<?> elementClass = ReflectionUtils.getCollectionElementClass(descriptor.getPropertyGetter().getGenericReturnType());
			if (elementClass != null && !ClassUtils.isConcrete(elementClass) 
					&& elementClass.getAnnotation(Editable.class) != null) {
				return new PropertyContext<List<Serializable>>(descriptor) {

					@Override
					public PropertyViewer renderForView(String componentId, final IModel<List<Serializable>> model) {
						return new PropertyViewer(componentId, descriptor) {

							@Override
							protected Component newContent(String id, PropertyDescriptor propertyDescriptor) {
								if (model.getObject() != null) {
									return new PolymorphicListPropertyViewer(id, propertyDescriptor, model.getObject());
								} else {
									return new EmptyValueLabel(id) {

										@Override
										protected AnnotatedElement getElement() {
											return propertyDescriptor.getPropertyGetter();
										}
										
									};
								}
							}
							
						};
					}

					@Override
					public PropertyEditor<List<Serializable>> renderForEdit(String componentId, IModel<List<Serializable>> model) {
						return new PolymorphicListPropertyEditor(componentId, descriptor, model);
					}
					
				};
			}
		}		
		return null;
	}

	@Override
	public int getPriority() {
		return DEFAULT_PRIORITY;
	}
	
}
