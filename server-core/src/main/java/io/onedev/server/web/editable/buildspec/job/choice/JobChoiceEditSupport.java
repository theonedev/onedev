package io.onedev.server.web.editable.buildspec.job.choice;

import java.lang.reflect.Method;
import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.IModel;
import org.eclipse.jgit.util.StringUtils;

import io.onedev.server.util.ReflectionUtils;
import io.onedev.server.web.editable.EditSupport;
import io.onedev.server.web.editable.EmptyValueLabel;
import io.onedev.server.web.editable.PropertyContext;
import io.onedev.server.web.editable.PropertyDescriptor;
import io.onedev.server.web.editable.PropertyEditor;
import io.onedev.server.web.editable.PropertyViewer;
import io.onedev.server.web.editable.annotation.JobChoice;

@SuppressWarnings("serial")
public class JobChoiceEditSupport implements EditSupport {

	@Override
	public PropertyContext<?> getEditContext(PropertyDescriptor descriptor) {
        Method propertyGetter = descriptor.getPropertyGetter();
        JobChoice jobChoice = propertyGetter.getAnnotation(JobChoice.class);
        if (jobChoice != null) {
        	if (List.class.isAssignableFrom(propertyGetter.getReturnType()) 
        			&& ReflectionUtils.getCollectionElementType(propertyGetter.getGenericReturnType()) == String.class) {
        		return new PropertyContext<List<String>>(descriptor) {

					@Override
					public PropertyViewer renderForView(String componentId, final IModel<List<String>> model) {
						return new PropertyViewer(componentId, descriptor) {

							@Override
							protected Component newContent(String id, PropertyDescriptor propertyDescriptor) {
						        List<String> jobNames = model.getObject();
						        if (jobNames != null && !jobNames.isEmpty()) {
						            return new Label(id, StringUtils.join(jobNames, ", " ));
						        } else {
									return new EmptyValueLabel(id, propertyDescriptor.getPropertyGetter());
						        }
							}
							
						};
					}

					@Override
					public PropertyEditor<List<String>> renderForEdit(String componentId, IModel<List<String>> model) {
						return new JobMultiChoiceEditor(componentId, descriptor, model);
					}
        			
        		};
        	} else if (propertyGetter.getReturnType() == String.class) {
        		return new PropertyContext<String>(descriptor) {

					@Override
					public PropertyViewer renderForView(String componentId, final IModel<String> model) {
						return new PropertyViewer(componentId, descriptor) {

							@Override
							protected Component newContent(String id, PropertyDescriptor propertyDescriptor) {
						        String jobName = model.getObject();
						        if (jobName != null) {
						            return new Label(id, jobName);
						        } else {
									return new EmptyValueLabel(id, propertyDescriptor.getPropertyGetter());
						        }
							}
							
						};
					}

					@Override
					public PropertyEditor<String> renderForEdit(String componentId, IModel<String> model) {
						return new JobSingleChoiceEditor(componentId, descriptor, model);
					}
        			
        		};
        	} else {
        		throw new RuntimeException("Annotation 'JobChoice' should be applied to property with type "
        				+ "'List<String> or String'");
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
