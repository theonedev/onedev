package io.onedev.server.web.editable.issue.choice;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;

import org.apache.wicket.Component;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.IModel;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.IssueManager;
import io.onedev.server.model.Issue;
import io.onedev.server.model.Project;
import io.onedev.server.web.editable.EditSupport;
import io.onedev.server.web.editable.EmptyValueLabel;
import io.onedev.server.web.editable.PropertyContext;
import io.onedev.server.web.editable.PropertyDescriptor;
import io.onedev.server.web.editable.PropertyEditor;
import io.onedev.server.web.editable.PropertyViewer;
import io.onedev.server.annotation.IssueChoice;

@SuppressWarnings("serial")
public class IssueChoiceEditSupport implements EditSupport {

	@Override
	public PropertyContext<?> getEditContext(PropertyDescriptor descriptor) {
        Method propertyGetter = descriptor.getPropertyGetter();
        IssueChoice issueChoice = propertyGetter.getAnnotation(IssueChoice.class);
        if (issueChoice != null) {
        	if (propertyGetter.getReturnType() == Long.class) {
        		return new PropertyContext<Long>(descriptor) {

					@Override
					public PropertyViewer renderForView(String componentId, final IModel<Long> model) {
						return new PropertyViewer(componentId, descriptor) {

							@Override
							protected Component newContent(String id, PropertyDescriptor propertyDescriptor) {
								Long issueId = model.getObject();
								if (issueId != null) {
									Issue issue = OneDev.getInstance(IssueManager.class).get(issueId);
									if (issue != null) {
										if (Project.get() != null && Project.get().getForkRoot().equals(issue.getNumberScope()))
											return new Label(id, "#" + issue.getNumber());
										else
											return new Label(id, issue.getFQN().toString());
									} else {
										return new Label(id, "<i>Not Found</i>").setEscapeModelStrings(false);
									}
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
					public PropertyEditor<Long> renderForEdit(String componentId, IModel<Long> model) {
						return new IssueChoiceEditor(componentId, descriptor, model);
					}
        			
        		};
        	} else {
        		throw new RuntimeException("Annotation 'IssueChoice' should be applied to property with type 'Long'.");
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
