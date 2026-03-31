package io.onedev.server.web.editable.numberlist;

import io.onedev.server.web.editable.*;
import org.apache.wicket.Component;
import org.apache.wicket.model.IModel;

import java.lang.reflect.AnnotatedElement;
import java.util.List;

import static io.onedev.server.util.ReflectionUtils.getCollectionElementClass;

public class NumberListEditSupport implements EditSupport {

	@Override
	public PropertyContext<?> getEditContext(PropertyDescriptor descriptor) {
		var propertyGetter = descriptor.getPropertyGetter();
		if (List.class.isAssignableFrom(descriptor.getPropertyClass())) {
			var elementClass = getCollectionElementClass(propertyGetter.getGenericReturnType());
			if (elementClass == Integer.class || elementClass == Long.class) {
				return new PropertyContext<List<Number>>(descriptor) {

					@Override
					public PropertyViewer renderForView(String componentId, final IModel<List<Number>> model) {
						return new PropertyViewer(componentId, descriptor) {

							@Override
							protected Component newContent(String id, PropertyDescriptor propertyDescriptor) {
								if (model.getObject() != null && !model.getObject().isEmpty()) {
									return new NumberListPropertyViewer(id, model.getObject());
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
					public PropertyEditor<List<Number>> renderForEdit(String componentId, IModel<List<Number>> model) {
						return new NumberListEditor(componentId, descriptor, model);
					}

				};
			}
		}
		return null;
	}

	@Override
	public int getPriority() {
		return DEFAULT_PRIORITY * 2;
	}

}
