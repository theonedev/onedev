package io.onedev.server.web.editable.issue;

import java.lang.reflect.Method;

import org.apache.wicket.Component;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;

import com.google.common.base.Preconditions;

import io.onedev.server.model.Project;
import io.onedev.server.web.behavior.IssueQueryBehavior;
import io.onedev.server.web.behavior.inputassist.InputAssistBehavior;
import io.onedev.server.web.editable.EditSupport;
import io.onedev.server.web.editable.EmptyValueLabel;
import io.onedev.server.web.editable.PropertyContext;
import io.onedev.server.web.editable.PropertyDescriptor;
import io.onedev.server.web.editable.PropertyEditor;
import io.onedev.server.web.editable.PropertyViewer;
import io.onedev.server.web.editable.annotation.IssueQuery;
import io.onedev.server.web.editable.string.StringPropertyEditor;

@SuppressWarnings("serial")
public class IssueQueryEditSupport implements EditSupport {

	@Override
	public PropertyContext<?> getEditContext(PropertyDescriptor descriptor) {
		Method propertyGetter = descriptor.getPropertyGetter();
		IssueQuery issueQuery = propertyGetter.getAnnotation(IssueQuery.class);
        if (issueQuery != null) {
        	if (propertyGetter.getReturnType() != String.class) {
	    		throw new RuntimeException("Annotation 'IssueQuery' should be applied to property "
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
								return new EmptyValueLabel(id, propertyDescriptor.getPropertyGetter());
					        }
						}
						
					};
				}

				@Override
				public PropertyEditor<String> renderForEdit(String componentId, IModel<String> model) {
		        	return new StringPropertyEditor(componentId, descriptor, model) {
		        		
		        		@Override
		        		protected InputAssistBehavior getInputAssistBehavior() {
		        			IssueQuery issueQuery = Preconditions.checkNotNull(
		        					getDescriptor().getPropertyGetter().getAnnotation(IssueQuery.class));
		        	        return new IssueQueryBehavior(new AbstractReadOnlyModel<Project>() {

		        				@Override
		        				public Project getObject() {
		        					return Project.get();
		        				}
		        	    		
		        	    	}, issueQuery.withOrder(), issueQuery.withCurrentUserCriteria(), issueQuery.withCurrentBuildCriteria(), 
		        	        		issueQuery.withCurrentPullRequestCriteria(), issueQuery.withCurrentCommitCriteria());
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
