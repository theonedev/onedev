package io.onedev.server.web.editable.code;

import java.io.Serializable;
import java.lang.reflect.AnnotatedElement;
import java.util.List;

import io.onedev.commons.utils.StringUtils;
import org.apache.wicket.Component;
import org.apache.wicket.model.IModel;

import io.onedev.commons.utils.ExplicitException;
import io.onedev.server.util.ReflectionUtils;
import io.onedev.server.annotation.Code;
import io.onedev.server.web.editable.EditSupport;
import io.onedev.server.web.editable.EmptyValueLabel;
import io.onedev.server.web.editable.PropertyContext;
import io.onedev.server.web.editable.PropertyDescriptor;
import io.onedev.server.web.editable.PropertyEditor;
import io.onedev.server.web.editable.PropertyViewer;

@SuppressWarnings("serial")
public class CodeEditSupport implements EditSupport {

	@Override
	public PropertyContext<?> getEditContext(PropertyDescriptor descriptor) {
		Class<?> propertyClass = descriptor.getPropertyGetter().getReturnType();
		Code annotation = descriptor.getPropertyGetter().getAnnotation(Code.class);
		if (annotation != null) {
			if (propertyClass == String.class || List.class.isAssignableFrom(propertyClass) && ReflectionUtils.getCollectionElementClass(descriptor.getPropertyGetter().getGenericReturnType()) == String.class) {
				return new PropertyContext<Serializable>(descriptor) {

					@Override
					public PropertyViewer renderForView(String componentId, final IModel<Serializable> model) {
						return new PropertyViewer(componentId, descriptor) {

							@Override
							protected Component newContent(String id, PropertyDescriptor propertyDescriptor) {
						        Serializable modelObject = model.getObject();
								if (modelObject instanceof String && StringUtils.isNotBlank((String)modelObject)
										|| modelObject instanceof List && !((List<?>) modelObject).isEmpty()) {
									return new CodePropertyViewer(id, model.getObject(), annotation.language());
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
					public PropertyEditor<Serializable> renderForEdit(String componentId, IModel<Serializable> model) {
						return new CodePropertyEditor(componentId, descriptor, model);
					}
					
				};
			} else {
				throw new ExplicitException("@Code annotation should be applied to a String or List<String> property");
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
