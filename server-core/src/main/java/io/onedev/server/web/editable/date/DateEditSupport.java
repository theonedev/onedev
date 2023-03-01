package io.onedev.server.web.editable.date;

import java.lang.reflect.AnnotatedElement;
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
import io.onedev.server.annotation.WithTime;

@SuppressWarnings("serial")
public class DateEditSupport implements EditSupport {

	public static final String DATE_INPUT_FORMAT = "yyyy-MM-dd";
	
	@Override
	public PropertyContext<?> getEditContext(PropertyDescriptor descriptor) {
		Class<?> propertyClass = descriptor.getPropertyGetter().getReturnType();
		if (propertyClass == Date.class) {
			boolean withTime = descriptor.getPropertyGetter().getAnnotation(WithTime.class) != null;
			return new PropertyContext<Date>(descriptor) {

				@Override
				public PropertyViewer renderForView(String componentId, final IModel<Date> model) {
					return new PropertyViewer(componentId, descriptor) {

						@Override
						protected Component newContent(String id, PropertyDescriptor propertyDescriptor) {
							if (model.getObject() != null) {
								if (withTime)
									return new Label(id, DateUtils.formatDateTime(model.getObject()));
								else
									return new Label(id, DateUtils.formatDate(model.getObject()));
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
				public PropertyEditor<Date> renderForEdit(String componentId, IModel<Date> model) {
					return new DatePropertyEditor(componentId, descriptor, model, withTime);
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
