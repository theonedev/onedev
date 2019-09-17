package io.onedev.server.web.editable.buildchoice;

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
import io.onedev.server.web.editable.annotation.BuildChoice;

@SuppressWarnings("serial")
public class BuildEditSupport implements EditSupport {

	@Override
	public PropertyContext<?> getEditContext(PropertyDescriptor descriptor) {
        Method propertyGetter = descriptor.getPropertyGetter();
        BuildChoice buildChoice = propertyGetter.getAnnotation(BuildChoice.class);
        if (buildChoice != null) {
        	if (propertyGetter.getReturnType() == Long.class) {
        		return new PropertyContext<Long>(descriptor) {

					@Override
					public PropertyViewer renderForView(String componentId, final IModel<Long> model) {
						return new PropertyViewer(componentId, descriptor) {

							@Override
							protected Component newContent(String id, PropertyDescriptor propertyDescriptor) {
								if (model.getObject() != null) 
						            return new Label(id, "#" + model.getObject());
						        else 
									return new EmptyValueLabel(id, propertyDescriptor.getPropertyGetter());
							}
							
						};
					}

					@Override
					public PropertyEditor<Long> renderForEdit(String componentId, IModel<Long> model) {
						return new BuildChoiceEditor(componentId, descriptor, model);
					}
        			
        		};
        	} else {
        		throw new RuntimeException("Annotation 'BuildChoice' should be applied to property with type 'Long'.");
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
