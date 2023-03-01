package io.onedev.server.web.editable.refreshtoken;

import java.lang.reflect.AnnotatedElement;

import org.apache.wicket.Component;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.IModel;

import io.onedev.server.web.editable.EditSupport;
import io.onedev.server.web.editable.EmptyValueLabel;
import io.onedev.server.web.editable.PropertyContext;
import io.onedev.server.web.editable.PropertyDescriptor;
import io.onedev.server.web.editable.PropertyEditor;
import io.onedev.server.web.editable.PropertyViewer;
import io.onedev.server.annotation.RefreshToken;

@SuppressWarnings("serial")
public class RefreshTokenEditSupport implements EditSupport {

	@Override
	public PropertyContext<?> getEditContext(PropertyDescriptor descriptor) {
		if (descriptor.getPropertyClass() == String.class) {
			RefreshToken refreshToken = descriptor.getPropertyGetter().getAnnotation(RefreshToken.class);
			if (refreshToken != null) {
				return new PropertyContext<String>(descriptor) {

					@Override
					public PropertyViewer renderForView(String componentId, final IModel<String> model) {
						return new PropertyViewer(componentId, descriptor) {

							@Override
							protected Component newContent(String id, PropertyDescriptor propertyDescriptor) {
								if (model.getObject() != null) {
									return new Label(id, "******");
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
					public PropertyEditor<String> renderForEdit(String componentId, IModel<String> model) {
						return new RefreshTokenPropertyEditor(componentId, descriptor, model);
					}
					
				};
			} else {
				return null;
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
