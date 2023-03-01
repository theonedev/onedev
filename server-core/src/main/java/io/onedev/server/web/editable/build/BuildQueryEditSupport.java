package io.onedev.server.web.editable.build;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;

import org.apache.wicket.Component;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;

import io.onedev.server.model.Project;
import io.onedev.server.web.behavior.BuildQueryBehavior;
import io.onedev.server.web.behavior.inputassist.InputAssistBehavior;
import io.onedev.server.web.editable.EditSupport;
import io.onedev.server.web.editable.EmptyValueLabel;
import io.onedev.server.web.editable.PropertyContext;
import io.onedev.server.web.editable.PropertyDescriptor;
import io.onedev.server.web.editable.PropertyEditor;
import io.onedev.server.web.editable.PropertyViewer;
import io.onedev.server.annotation.BuildQuery;
import io.onedev.server.web.editable.string.StringPropertyEditor;

@SuppressWarnings("serial")
public class BuildQueryEditSupport implements EditSupport {

	@Override
	public PropertyContext<?> getEditContext(PropertyDescriptor descriptor) {
		Method propertyGetter = descriptor.getPropertyGetter();
		BuildQuery buildQuery = propertyGetter.getAnnotation(BuildQuery.class);
        if (buildQuery != null) {
        	if (propertyGetter.getReturnType() != String.class) {
	    		throw new RuntimeException("Annotation 'BuildQuery' should be applied to property "
	    				+ "with type 'String'");
        	}
    		return new PropertyContext<String>(descriptor) {

				@Override
				public PropertyViewer renderForView(String componentId, final IModel<String> model) {
					return new PropertyViewer(componentId, descriptor) {

						@Override
						protected Component newContent(String id, PropertyDescriptor propertyDescriptor) {
					        String query = model.getObject();
					        if (query != null) {
					        	return new Label(id, query);
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
					InputAssistBehavior inputAssist = new BuildQueryBehavior(new AbstractReadOnlyModel<Project>() {

						@Override
						public Project getObject() {
							return Project.get();
						}
			    		
			    	}, buildQuery.withOrder(), buildQuery.withCurrentUserCriteria(), buildQuery.withUnfinishedCriteria());
					
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
