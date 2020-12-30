package io.onedev.server.web.editable.code;

import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.model.IModel;

import io.onedev.commons.utils.ExplicitException;
import io.onedev.server.util.ReflectionUtils;
import io.onedev.server.web.editable.EditSupport;
import io.onedev.server.web.editable.EmptyValueLabel;
import io.onedev.server.web.editable.PropertyContext;
import io.onedev.server.web.editable.PropertyDescriptor;
import io.onedev.server.web.editable.PropertyEditor;
import io.onedev.server.web.editable.PropertyViewer;
import io.onedev.server.web.editable.annotation.Code;

@SuppressWarnings("serial")
public class CodeEditSupport implements EditSupport {

	@Override
	public PropertyContext<?> getEditContext(PropertyDescriptor descriptor) {
		Class<?> propertyClass = descriptor.getPropertyGetter().getReturnType();
		Code annotation = descriptor.getPropertyGetter().getAnnotation(Code.class);
		if (annotation != null) {
			if (List.class.isAssignableFrom(propertyClass) && ReflectionUtils.getCollectionElementType(descriptor.getPropertyGetter().getGenericReturnType()) == String.class) {
				return new PropertyContext<List<String>>(descriptor) {

					@Override
					public PropertyViewer renderForView(String componentId, final IModel<List<String>> model) {
						return new PropertyViewer(componentId, descriptor) {

							@Override
							protected Component newContent(String id, PropertyDescriptor propertyDescriptor) {
						        List<String> code = model.getObject();
								if (code != null && !code.isEmpty()) {
									return new CodePropertyViewer(id, model.getObject(), annotation.language());
								} else {
									return new EmptyValueLabel(id, propertyDescriptor.getPropertyGetter());
								}
							}
							
						};
					}

					@Override
					public PropertyEditor<List<String>> renderForEdit(String componentId, IModel<List<String>> model) {
						return new CodePropertyEditor(componentId, descriptor, model);
					}
					
				};
			} else {
				throw new ExplicitException("@Code annotation should only be applied to a List<String> property");
			}
		} else {
			return null;
		}
	}

	@Override
	public int getPriority() {
		return DEFAULT_PRIORITY;
	}
	
}
