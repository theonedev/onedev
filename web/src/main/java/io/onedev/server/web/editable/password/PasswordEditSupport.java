package io.onedev.server.web.editable.password;

import org.apache.wicket.Component;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.IModel;

import io.onedev.server.util.editable.annotation.Password;
import io.onedev.server.web.editable.EditSupport;
import io.onedev.server.web.editable.EmptyValueLabel;
import io.onedev.server.web.editable.PropertyContext;
import io.onedev.server.web.editable.PropertyDescriptor;
import io.onedev.server.web.editable.PropertyEditor;
import io.onedev.server.web.editable.PropertyViewer;

@SuppressWarnings("serial")
public class PasswordEditSupport implements EditSupport {

	@Override
	public PropertyContext<?> getEditContext(Class<?> beanClass, String propertyName) {
		PropertyDescriptor propertyDescriptor = new PropertyDescriptor(beanClass, propertyName);
		if (propertyDescriptor.getPropertyClass() == String.class) {
			Password password = propertyDescriptor.getPropertyGetter().getAnnotation(Password.class);
			if (password != null) {
				if (password.confirmative()) {
					return new PropertyContext<String>(propertyDescriptor) {

						@Override
						public PropertyViewer renderForView(String componentId, final IModel<String> model) {
							return new PropertyViewer(componentId, this) {

								@Override
								protected Component newContent(String id, PropertyDescriptor propertyDescriptor) {
									if (model.getObject() != null) {
										return new Label(id, "******");
									} else {
										return new EmptyValueLabel(id, propertyDescriptor.getPropertyGetter());
									}
								}
								
							};
						}

						@Override
						public PropertyEditor<String> renderForEdit(String componentId, IModel<String> model) {
							return new ConfirmativePasswordPropertyEditor(componentId, this, model);
						}
						
					};
				} else {
					return new PropertyContext<String>(propertyDescriptor) {

						@Override
						public PropertyViewer renderForView(String componentId, final IModel<String> model) {
							return new PropertyViewer(componentId, this) {

								@Override
								protected Component newContent(String id, PropertyDescriptor propertyDescriptor) {
									if (model.getObject() != null) {
										return new Label(id, "******");
									} else {
										return new EmptyValueLabel(id, propertyDescriptor.getPropertyGetter());
									}
								}
								
							};
						}

						@Override
						public PropertyEditor<String> renderForEdit(String componentId, IModel<String> model) {
							return new PasswordPropertyEditor(componentId, this, model);
						}
						
					};
				}
			} else {
				return null;
			}
		} else {
			return null;
		}
	}

}
