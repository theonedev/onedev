package io.onedev.server.web.editable;

import java.lang.reflect.Method;

import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;

import io.onedev.server.model.Project;
import io.onedev.server.web.behavior.NotificationReceiverBehavior;
import io.onedev.server.web.behavior.inputassist.InputAssistBehavior;
import io.onedev.server.annotation.NotificationReceiver;
import io.onedev.server.web.editable.string.StringPropertyEditor;
import io.onedev.server.web.editable.string.StringPropertyViewer;

@SuppressWarnings("serial")
public class NotificationReceiverEditSupport implements EditSupport {

	@Override
	public PropertyContext<?> getEditContext(PropertyDescriptor descriptor) {
		Method propertyGetter = descriptor.getPropertyGetter();
        if (propertyGetter.getAnnotation(NotificationReceiver.class) != null) {
        	if (propertyGetter.getReturnType() != String.class) {
	    		throw new RuntimeException("Annotation 'NotificationReceiver' should be applied to property "
	    				+ "of type 'String'.");
        	}
    		return new PropertyContext<String>(descriptor) {

				@Override
				public PropertyViewer renderForView(String componentId, IModel<String> model) {
					return new StringPropertyViewer(componentId, descriptor, model.getObject());
				}

				@Override
				public PropertyEditor<String> renderForEdit(String componentId, IModel<String> model) {
					InputAssistBehavior inputAssist = new NotificationReceiverBehavior(
							new AbstractReadOnlyModel<Project>() {

						@Override
						public Project getObject() {
							return Project.get();
						}
						
					});
		        	return new StringPropertyEditor(componentId, descriptor, model).setInputAssist(inputAssist);
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
