package io.onedev.server.web.editable.stringlist;

import io.onedev.server.web.editable.*;
import org.apache.wicket.Component;
import org.apache.wicket.model.IModel;

import java.lang.reflect.AnnotatedElement;
import java.util.List;

import static io.onedev.server.util.ReflectionUtils.getCollectionElementClass;

public class StringListEditSupport implements EditSupport {

	@Override
	public PropertyContext<?> getEditContext(PropertyDescriptor descriptor) {
		var propertyGetter = descriptor.getPropertyGetter();
		if (List.class.isAssignableFrom(descriptor.getPropertyClass()) 
				&& getCollectionElementClass(propertyGetter.getGenericReturnType()) == String.class) {
						
			return new PropertyContext<List<String>>(descriptor) {

				@Override
				public PropertyViewer renderForView(String componentId, final IModel<List<String>> model) {

					return new PropertyViewer(componentId, descriptor) {

						@Override
						protected Component newContent(String id, PropertyDescriptor propertyDescriptor) {
							if (model.getObject() != null && !model.getObject().isEmpty()) {
								return new StringListPropertyViewer(id, propertyDescriptor, model.getObject());
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
				public PropertyEditor<List<String>> renderForEdit(String componentId, IModel<List<String>> model) {
					return new StringListEditor(componentId, descriptor, model);
				}
				
			};
		} else {
			return null;
		}
		
	}

	@Override
	public int getPriority() {
		return DEFAULT_PRIORITY * 2;
	}
	
}
