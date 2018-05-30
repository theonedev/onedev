package io.onedev.server.web.editable.script;

import org.apache.wicket.Component;
import org.apache.wicket.model.IModel;

import com.google.common.base.Preconditions;

import io.onedev.server.web.editable.EditSupport;
import io.onedev.server.web.editable.EmptyValueLabel;
import io.onedev.server.web.editable.PropertyContext;
import io.onedev.server.web.editable.PropertyDescriptor;
import io.onedev.server.web.editable.PropertyEditor;
import io.onedev.server.web.editable.PropertyViewer;
import io.onedev.server.web.editable.annotation.Script;

@SuppressWarnings("serial")
public class ScriptEditSupport implements EditSupport {

	@Override
	public PropertyContext<?> getEditContext(Class<?> beanClass, String propertyName) {
		PropertyDescriptor propertyDescriptor = new PropertyDescriptor(beanClass, propertyName);

		Class<?> propertyClass = propertyDescriptor.getPropertyGetter().getReturnType();
		if (propertyDescriptor.getPropertyGetter().getAnnotation(Script.class) != null) {
			Preconditions.checkState(propertyClass == String.class, 
					"@Code annotation should only be applied to a string property");
			return new PropertyContext<String>(propertyDescriptor) {

				@Override
				public PropertyViewer renderForView(String componentId, final IModel<String> model) {
					return new PropertyViewer(componentId, this) {

						@Override
						protected Component newContent(String id, PropertyDescriptor propertyDescriptor) {
							if (model.getObject() != null) {
								return new ScriptPropertyViewer(id, model.getObject());
							} else {
								return new EmptyValueLabel(id, propertyDescriptor.getPropertyGetter());
							}
						}
						
					};
				}

				@Override
				public PropertyEditor<String> renderForEdit(String componentId, IModel<String> model) {
					return new ScriptPropertyEditor(componentId, this, model);
				}
				
			};
		} else {
			return null;
		}
	}

}
