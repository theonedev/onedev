package io.onedev.server.web.editable.date;

import java.util.Date;

import org.apache.wicket.Component;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.IModel;

import io.onedev.server.util.editable.annotation.NameOfEmptyValue;
import io.onedev.server.web.editable.EditSupport;
import io.onedev.server.web.editable.EmptyValueLabel;
import io.onedev.server.web.editable.PropertyContext;
import io.onedev.server.web.editable.PropertyDescriptor;
import io.onedev.server.web.editable.PropertyEditor;
import io.onedev.server.web.editable.PropertyViewer;

@SuppressWarnings("serial")
public class DateEditSupport implements EditSupport {

	public static final String DATE_INPUT_FORMAT = "yyyy-MM-dd";
	
	@Override
	public PropertyContext<?> getEditContext(Class<?> beanClass, String propertyName) {
		PropertyDescriptor propertyDescriptor = new PropertyDescriptor(beanClass, propertyName);

		Class<?> propertyClass = propertyDescriptor.getPropertyGetter().getReturnType();
		if (propertyClass == Date.class) {
			return new PropertyContext<Date>(propertyDescriptor) {

				@Override
				public PropertyViewer renderForView(String componentId, final IModel<Date> model) {
					return new PropertyViewer(componentId, this) {

						@Override
						protected Component newContent(String id, PropertyDescriptor propertyDescriptor) {
							if (model.getObject() != null) {
								return new Label(id, model.getObject());
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
				public PropertyEditor<Date> renderForEdit(String componentId, IModel<Date> model) {
					return new DatePropertyEditor(componentId, this, model);
				}
				
			};
		} else {
			return null;
		}
		
	}

}
