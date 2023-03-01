package io.onedev.server.web.editable;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;

import org.apache.wicket.Component;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;

import io.onedev.server.model.Project;
import io.onedev.server.web.behavior.CodeCommentQueryBehavior;
import io.onedev.server.web.behavior.inputassist.InputAssistBehavior;
import io.onedev.server.annotation.CodeCommentQuery;
import io.onedev.server.web.editable.string.StringPropertyEditor;
import io.onedev.server.web.page.project.ProjectPage;
import io.onedev.server.web.util.WicketUtils;

@SuppressWarnings("serial")
public class CodeCommentQueryEditSupport implements EditSupport {

	@Override
	public PropertyContext<?> getEditContext(PropertyDescriptor descriptor) {
		Method propertyGetter = descriptor.getPropertyGetter();
		CodeCommentQuery codeCommentQuery = propertyGetter.getAnnotation(CodeCommentQuery.class);
        if (codeCommentQuery != null) {
        	if (propertyGetter.getReturnType() != String.class) {
	    		throw new RuntimeException("Annotation 'CodeCommentQuery' should be applied to property "
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
					InputAssistBehavior inputAssist = new CodeCommentQueryBehavior(new AbstractReadOnlyModel<Project>() {

						@Override
						public Project getObject() {
							return ((ProjectPage) WicketUtils.getPage()).getProject();
						}
			    		
			    	}, codeCommentQuery.withCurrentUserCriteria(), codeCommentQuery.withOrder());
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
