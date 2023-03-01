package io.onedev.server.web.editable.numeric;

import java.lang.reflect.AnnotatedElement;
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
import io.onedev.server.annotation.BuildChoice;
import io.onedev.server.annotation.IssueChoice;
import io.onedev.server.annotation.PullRequestChoice;
import io.onedev.server.annotation.WorkingPeriod;

@SuppressWarnings("serial")
public class NumericEditSupport implements EditSupport {

	@Override
	public PropertyContext<?> getEditContext(PropertyDescriptor descriptor) {
		Method propertyGetter = descriptor.getPropertyGetter();
		Class<?> propertyClass = propertyGetter.getReturnType();
		if ((propertyClass == int.class || propertyClass == long.class 
				|| propertyClass == Integer.class || propertyClass == Long.class 
				|| propertyClass == Float.class || propertyClass == float.class
				|| propertyClass == Double.class || propertyClass == double.class)
				&& propertyGetter.getAnnotation(IssueChoice.class) == null
				&& propertyGetter.getAnnotation(PullRequestChoice.class) == null
				&& propertyGetter.getAnnotation(BuildChoice.class) == null
				&& propertyGetter.getAnnotation(WorkingPeriod.class) == null) {
			return new PropertyContext<Number>(descriptor) {

				@Override
				public PropertyViewer renderForView(String componentId, final IModel<Number> model) {
					return new PropertyViewer(componentId, descriptor) {

						@Override
						protected Component newContent(String id, PropertyDescriptor propertyDescriptor) {
							if (model.getObject() != null) {
								return new Label(id, model.getObject().toString());
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
				public PropertyEditor<Number> renderForEdit(String componentId, IModel<Number> model) {
					return new NumericPropertyEditor(componentId, descriptor, model);
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
