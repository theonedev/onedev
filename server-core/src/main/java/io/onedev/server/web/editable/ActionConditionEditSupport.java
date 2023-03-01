package io.onedev.server.web.editable;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;

import org.apache.wicket.Component;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.IModel;

import io.onedev.server.web.behavior.ActionConditionBehavior;
import io.onedev.server.annotation.ActionCondition;
import io.onedev.server.web.editable.string.StringPropertyEditor;

@SuppressWarnings("serial")
public class ActionConditionEditSupport implements EditSupport {

	@Override
	public PropertyContext<?> getEditContext(PropertyDescriptor descriptor) {
		Method propertyGetter = descriptor.getPropertyGetter();
        if (propertyGetter.getAnnotation(ActionCondition.class) != null) {
        	if (propertyGetter.getReturnType() != String.class) {
	    		throw new RuntimeException("Annotation 'ActionCondition' should be applied to property "
	    				+ "with type 'String'");
        	}
    		return new PropertyContext<String>(descriptor) {

				@Override
				public PropertyViewer renderForView(String componentId, final IModel<String> model) {
					return new PropertyViewer(componentId, descriptor) {

						@Override
						protected Component newContent(String id, PropertyDescriptor propertyDescriptor) {
					        String condition = model.getObject();
					        if (condition != null) {
					        	return new Label(id, condition);
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
		        	return new StringPropertyEditor(componentId, descriptor, model)
		        			.setInputAssist(new ActionConditionBehavior());
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
