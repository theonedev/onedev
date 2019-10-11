package io.onedev.server.web.editable.bool;

import java.lang.reflect.Method;

import org.apache.wicket.Component;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.IModel;

import io.onedev.server.web.editable.EditSupport;
import io.onedev.server.web.editable.EmptyValueLabel;
import io.onedev.server.web.editable.PropertyContext;
import io.onedev.server.web.editable.PropertyDescriptor;
import io.onedev.server.web.editable.PropertyEditor;
import io.onedev.server.web.editable.PropertyViewer;
import io.onedev.server.web.editable.annotation.NameOfEmptyValue;
import io.onedev.server.web.util.TextUtils;

@SuppressWarnings("serial")
public class BooleanEditSupport implements EditSupport {

	@Override
	public PropertyContext<?> getEditContext(PropertyDescriptor descriptor) {
		Method propertyGetter = descriptor.getPropertyGetter();
		
		Class<?> propertyClass = propertyGetter.getReturnType();
		if (propertyClass == boolean.class || propertyClass == Boolean.class) {
			return new PropertyContext<Boolean>(descriptor) {

				@Override
				public PropertyViewer renderForView(String componentId, final IModel<Boolean> model) {
					return new PropertyViewer(componentId, descriptor) {

						@Override
						protected Component newContent(String id, PropertyDescriptor propertyDescriptor) {
							if (model.getObject() != null) {
								return new Label(id, TextUtils.describe(model.getObject()));
							} else {
								NameOfEmptyValue nameOfEmptyValue = propertyDescriptor.getPropertyGetter().getAnnotation(NameOfEmptyValue.class);
								if (nameOfEmptyValue != null)
									return new Label(id, nameOfEmptyValue.value());
								else 
									return new EmptyValueLabel(id, propertyDescriptor.getPropertyGetter());
							}
						}
						
					};
				}

				@Override
				public PropertyEditor<Boolean> renderForEdit(String componentId, IModel<Boolean> model) {
					if (descriptor.isPropertyRequired())
						return new BooleanPropertyEditor(componentId, descriptor, model);
					else
						return new NullableBooleanPropertyEditor(componentId, descriptor, model);
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
