package io.onedev.server.web.editable.issuechoice;

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
import io.onedev.server.web.editable.annotation.IssueChoice;

@SuppressWarnings("serial")
public class IssueEditSupport implements EditSupport {

	@Override
	public PropertyContext<?> getEditContext(Class<?> beanClass, String propertyName) {
		PropertyDescriptor propertyDescriptor = new PropertyDescriptor(beanClass, propertyName);
        Method propertyGetter = propertyDescriptor.getPropertyGetter();
        IssueChoice issueChoice = propertyGetter.getAnnotation(IssueChoice.class);
        if (issueChoice != null) {
        	if (propertyGetter.getReturnType() == Long.class) {
        		return new PropertyContext<Long>(propertyDescriptor) {

					@Override
					public PropertyViewer renderForView(String componentId, final IModel<Long> model) {
						return new PropertyViewer(componentId, this) {

							@Override
							protected Component newContent(String id, PropertyDescriptor propertyDescriptor) {
								if (model.getObject() != null) {
						            return new Label(id, "#" + model.getObject());
						        } else {
									return new EmptyValueLabel(id, propertyDescriptor.getPropertyGetter());
						        }
							}
							
						};
					}

					@Override
					public PropertyEditor<Long> renderForEdit(String componentId, IModel<Long> model) {
						return new IssueChoiceEditor(componentId, this, model);
					}
        			
        		};
        	} else {
        		throw new RuntimeException("Annotation 'IssueChoice' should be applied to property with type 'Long'.");
        	}
        } else {
            return null;
        }
	}

}
