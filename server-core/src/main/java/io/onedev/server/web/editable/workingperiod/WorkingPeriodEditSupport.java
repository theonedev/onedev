package io.onedev.server.web.editable.workingperiod;

import io.onedev.server.OneDev;
import io.onedev.server.annotation.WorkingPeriod;
import io.onedev.server.service.SettingService;
import io.onedev.server.web.editable.*;
import org.apache.wicket.Component;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.IModel;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;

public class WorkingPeriodEditSupport implements EditSupport {

	@Override
	public PropertyContext<?> getEditContext(PropertyDescriptor descriptor) {
		Method propertyGetter = descriptor.getPropertyGetter();
		if (propertyGetter.getAnnotation(WorkingPeriod.class) != null) {
			Class<?> propertyClass = propertyGetter.getReturnType();
			if (propertyClass == int.class || propertyClass == Integer.class) {
				return new PropertyContext<Integer>(descriptor) {
	
					@Override
					public PropertyViewer renderForView(String componentId, final IModel<Integer> model) {
						return new PropertyViewer(componentId, descriptor) {
	
							@Override
							protected Component newContent(String id, PropertyDescriptor propertyDescriptor) {
								if (model.getObject() != null) {
									var timeTrackingSetting = OneDev.getInstance(SettingService.class).getIssueSetting().getTimeTrackingSetting();
									return new Label(id, timeTrackingSetting.formatWorkingPeriod(model.getObject(), true));
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
					public PropertyEditor<Integer> renderForEdit(String componentId, IModel<Integer> model) {
						return new WorkingPeriodPropertyEditor(componentId, descriptor, model);
					}
					
				};
			} else {
				throw new RuntimeException("Annotation WorkingPeriod should be applied to property with type 'int' or 'Integer'");
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
