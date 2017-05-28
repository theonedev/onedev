package com.gitplex.server.web.editable.branchchoice;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.wicket.Component;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.IModel;
import org.eclipse.jgit.util.StringUtils;

import com.gitplex.server.util.editable.EditableUtils;
import com.gitplex.server.util.editable.annotation.BranchChoice;
import com.gitplex.server.web.editable.BeanContext;
import com.gitplex.server.web.editable.EditSupport;
import com.gitplex.server.web.editable.NotDefinedLabel;
import com.gitplex.server.web.editable.PropertyContext;
import com.gitplex.server.web.editable.PropertyDescriptor;
import com.gitplex.server.web.editable.PropertyEditor;
import com.gitplex.server.web.editable.PropertyViewer;

@SuppressWarnings("serial")
public class BranchEditSupport implements EditSupport {

	@Override
	public BeanContext<?> getBeanEditContext(Class<?> beanClass, Set<String> excludeProperties) {
		return null;
	}

	@Override
	public PropertyContext<?> getPropertyEditContext(Class<?> beanClass, String propertyName) {
		PropertyDescriptor propertyDescriptor = new PropertyDescriptor(beanClass, propertyName);
		Method propertyGetter = propertyDescriptor.getPropertyGetter();
        if (propertyGetter.getAnnotation(BranchChoice.class) != null) {
        	if (List.class.isAssignableFrom(propertyGetter.getReturnType()) 
        			&& EditableUtils.getElementClass(propertyGetter.getGenericReturnType()) == String.class) {
        		return new PropertyContext<List<String>>(propertyDescriptor) {

					@Override
					public PropertyViewer renderForView(String componentId, final IModel<List<String>> model) {
						return new PropertyViewer(componentId, this) {

							@Override
							protected Component newContent(String id, PropertyDescriptor propertyDescriptor) {
						        List<String> projectAndBranches = model.getObject();
						        if (projectAndBranches != null && !projectAndBranches.isEmpty()) {
						        	List<String> branches = new ArrayList<>();
						        	for (String each: projectAndBranches) {
						        		branches.add(each);
						        	}
						            return new Label(id, StringUtils.join(branches, ", " ));
						        } else {
									return new NotDefinedLabel(id);
						        }
							}
							
						};
					}

					@Override
					public PropertyEditor<List<String>> renderForEdit(String componentId, IModel<List<String>> model) {
			        	return new BranchMultiChoiceEditor(componentId, this, model);
					}
        			
        		};
        	} else if (propertyGetter.getReturnType() == String.class) {
        		return new PropertyContext<String>(propertyDescriptor) {

					@Override
					public PropertyViewer renderForView(String componentId, final IModel<String> model) {
						return new PropertyViewer(componentId, this) {

							@Override
							protected Component newContent(String id, PropertyDescriptor propertyDescriptor) {
						        String projectAndBranch = model.getObject();
						        if (projectAndBranch != null) {
						        	return new Label(id, projectAndBranch);
						        } else {
									return new NotDefinedLabel(id);
						        }
							}
							
						};
					}

					@Override
					public PropertyEditor<String> renderForEdit(String componentId, IModel<String> model) {
			        	return new BranchSingleChoiceEditor(componentId, this, model);
					}
        			
        		};
        	} else {
        		throw new RuntimeException("Annotation 'BranchChoice' should be applied to property "
        				+ "with type 'String' or 'List<String>'.");
        	}
        } else {
            return null;
        }
	}

}
