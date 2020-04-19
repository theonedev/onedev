package io.onedev.server.web.editable.date;

import java.util.Date;

import org.apache.wicket.Component;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.IModel;

import io.onedev.server.util.DateUtils;
import io.onedev.server.web.editable.EditSupport;
import io.onedev.server.web.editable.EmptyValueLabel;
import io.onedev.server.web.editable.PropertyContext;
import io.onedev.server.web.editable.PropertyDescriptor;
import io.onedev.server.web.editable.PropertyEditor;
import io.onedev.server.web.editable.PropertyViewer;
import io.onedev.server.web.editable.annotation.NameOfEmptyValue;

@SuppressWarnings("serial")
public class DateEditSupport implements EditSupport {

	public static final String DATE_INPUT_FORMAT = "yyyy-MM-dd";
	
	@Override
	public PropertyContext<?> getEditContext(PropertyDescriptor descriptor) {
		Class<?> propertyClass = descriptor.getPropertyGetter().getReturnType();
		if (propertyClass == Date.class) {
			return new PropertyContext<Date>(descriptor) {

				@Override
				public PropertyViewer renderForView(String componentId, final IModel<Date> model) {
					return new PropertyViewer(componentId, descriptor) {

						@Override
						protected Component newContent(String id, PropertyDescriptor propertyDescriptor) {
							if (model.getObject() != null) {
								return new Label(id, DateUtils.formatDate(model.getObject()));
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
					return new DatePropertyEditor(componentId, descriptor, model);
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
