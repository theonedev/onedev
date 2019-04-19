package io.onedev.server.web.editable;

import java.lang.reflect.Method;

import org.apache.wicket.model.IModel;

import io.onedev.server.web.behavior.UserMatcherBehavior;
import io.onedev.server.web.behavior.inputassist.InputAssistBehavior;
import io.onedev.server.web.editable.annotation.UserMatcher;
import io.onedev.server.web.editable.string.StringPropertyEditor;
import io.onedev.server.web.editable.string.StringPropertyViewer;

@SuppressWarnings("serial")
public class UserMatcherEditSupport implements EditSupport {

	@Override
	public PropertyContext<?> getEditContext(PropertyDescriptor descriptor) {
		Method propertyGetter = descriptor.getPropertyGetter();
        if (propertyGetter.getAnnotation(UserMatcher.class) != null) {
        	if (propertyGetter.getReturnType() != String.class) {
	    		throw new RuntimeException("Annotation 'UserMatcher' should be applied to property "
	    				+ "of type 'String'.");
        	}
    		return new PropertyContext<String>(descriptor) {

				@Override
				public PropertyViewer renderForView(String componentId, IModel<String> model) {
					return new StringPropertyViewer(componentId, descriptor, model.getObject());
				}

				@Override
				public PropertyEditor<String> renderForEdit(String componentId, IModel<String> model) {
		        	return new StringPropertyEditor(componentId, descriptor, model) {

						@Override
						protected InputAssistBehavior getInputAssistBehavior() {
							return new UserMatcherBehavior();
						}
		        		
		        	};
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
