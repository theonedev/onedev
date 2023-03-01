package io.onedev.server.web.editable.agentquery;

import java.lang.reflect.Method;

import org.apache.wicket.Component;
import org.apache.wicket.model.IModel;

import io.onedev.server.web.editable.EditSupport;
import io.onedev.server.web.editable.PropertyContext;
import io.onedev.server.web.editable.PropertyDescriptor;
import io.onedev.server.web.editable.PropertyEditor;
import io.onedev.server.web.editable.PropertyViewer;
import io.onedev.server.annotation.AgentQuery;

@SuppressWarnings("serial")
public class AgentQueryEditSupport implements EditSupport {

	@Override
	public PropertyContext<?> getEditContext(PropertyDescriptor descriptor) {
		Method propertyGetter = descriptor.getPropertyGetter();
		AgentQuery agentQuery = propertyGetter.getAnnotation(AgentQuery.class);
        if (agentQuery != null) {
        	if (propertyGetter.getReturnType() != String.class) {
	    		throw new RuntimeException("Annotation 'AgentQuery' should be applied to property "
	    				+ "with type 'String'");
        	}
    		return new PropertyContext<String>(descriptor) {

				@Override
				public PropertyViewer renderForView(String componentId, final IModel<String> model) {
					return new PropertyViewer(componentId, descriptor) {

						@Override
						protected Component newContent(String id, PropertyDescriptor propertyDescriptor) {
					        return new AgentQueryViewPanel(id, model.getObject());
						}
						
					};
				}

				@Override
				public PropertyEditor<String> renderForEdit(String componentId, IModel<String> model) {
		        	return new AgentQueryEditPanel(componentId, descriptor, model, agentQuery.forExecutor());
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
