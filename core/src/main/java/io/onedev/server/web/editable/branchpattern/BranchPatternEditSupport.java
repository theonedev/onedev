package io.onedev.server.web.editable.branchpattern;

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
import io.onedev.server.web.editable.annotation.BranchPattern;
import io.onedev.server.web.editable.annotation.NameOfEmptyValue;

@SuppressWarnings("serial")
public class BranchPatternEditSupport implements EditSupport {

	@Override
	public PropertyContext<?> getEditContext(PropertyDescriptor descriptor) {
		Method propertyGetter = descriptor.getPropertyGetter();
        if (propertyGetter.getAnnotation(BranchPattern.class) != null) {
        	if (propertyGetter.getReturnType() != String.class) {
	    		throw new RuntimeException("Annotation 'BranchPattern' should be applied to property "
	    				+ "with type 'String'.");
        	}
    		return new PropertyContext<String>(descriptor) {

				@Override
				public PropertyViewer renderForView(String componentId, final IModel<String> model) {
					return new PropertyViewer(componentId, descriptor) {

						@Override
						protected Component newContent(String id, PropertyDescriptor propertyDescriptor) {
					        String refMatch = model.getObject();
					        if (refMatch != null) {
					        	return new Label(id, refMatch);
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
				public PropertyEditor<String> renderForEdit(String componentId, IModel<String> model) {
		        	return new BranchPatternEditor(componentId, descriptor, model);
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
