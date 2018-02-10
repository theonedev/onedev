package com.turbodev.server.web.editable.password;

import java.util.Set;

import org.apache.wicket.Component;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.IModel;

import com.turbodev.server.util.editable.annotation.Password;
import com.turbodev.server.web.editable.BeanContext;
import com.turbodev.server.web.editable.EditSupport;
import com.turbodev.server.web.editable.NotDefinedLabel;
import com.turbodev.server.web.editable.PropertyContext;
import com.turbodev.server.web.editable.PropertyDescriptor;
import com.turbodev.server.web.editable.PropertyEditor;
import com.turbodev.server.web.editable.PropertyViewer;

@SuppressWarnings("serial")
public class PasswordEditSupport implements EditSupport {

	@Override
	public BeanContext<?> getBeanEditContext(Class<?> beanClass, Set<String> excludeProperties) {
		return null;
	}

	@Override
	public PropertyContext<?> getPropertyEditContext(Class<?> beanClass, String propertyName) {
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
										return new NotDefinedLabel(id);
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
										return new NotDefinedLabel(id);
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
