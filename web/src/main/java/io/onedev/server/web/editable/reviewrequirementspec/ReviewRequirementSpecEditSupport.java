package io.onedev.server.web.editable.reviewrequirementspec;

import java.lang.reflect.Method;

import org.apache.wicket.Component;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.IModel;

import io.onedev.server.util.editable.annotation.ReviewRequirementSpec;
import io.onedev.server.web.editable.EditSupport;
import io.onedev.server.web.editable.EmptyValueLabel;
import io.onedev.server.web.editable.PropertyContext;
import io.onedev.server.web.editable.PropertyDescriptor;
import io.onedev.server.web.editable.PropertyEditor;
import io.onedev.server.web.editable.PropertyViewer;

@SuppressWarnings("serial")
public class ReviewRequirementSpecEditSupport implements EditSupport {

	@Override
	public PropertyContext<?> getEditContext(Class<?> beanClass, String propertyName) {
		PropertyDescriptor propertyDescriptor = new PropertyDescriptor(beanClass, propertyName);
		Method propertyGetter = propertyDescriptor.getPropertyGetter();
        if (propertyGetter.getAnnotation(ReviewRequirementSpec.class) != null) {
        	if (propertyGetter.getReturnType() != String.class) {
	    		throw new RuntimeException("Annotation 'ReviewRequirementSpec' should be applied to property "
	    				+ "with type 'String'.");
        	}
    		return new PropertyContext<String>(propertyDescriptor) {

				@Override
				public PropertyViewer renderForView(String componentId, final IModel<String> model) {
					return new PropertyViewer(componentId, this) {

						@Override
						protected Component newContent(String id, PropertyDescriptor propertyDescriptor) {
					        String expr = model.getObject();
					        if (expr != null) {
					        	return new Label(id, expr);
					        } else {
								return new EmptyValueLabel(id, propertyDescriptor.getPropertyGetter());
					        }
						}
						
					};
				}

				@Override
				public PropertyEditor<String> renderForEdit(String componentId, IModel<String> model) {
		        	return new ReviewRequirementSpecEditor(componentId, this, model);
				}
    			
    		};
        } else {
            return null;
        }
	}

}
