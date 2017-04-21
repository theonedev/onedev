package com.gitplex.server.web.editable.branchpattern;

import java.lang.reflect.Method;
import java.util.Set;

import org.apache.wicket.Component;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.IModel;

import com.gitplex.server.util.editable.annotation.BranchPattern;
import com.gitplex.server.web.editable.BeanContext;
import com.gitplex.server.web.editable.EditSupport;
import com.gitplex.server.web.editable.NotDefinedLabel;
import com.gitplex.server.web.editable.PropertyContext;
import com.gitplex.server.web.editable.PropertyDescriptor;
import com.gitplex.server.web.editable.PropertyEditor;
import com.gitplex.server.web.editable.PropertyViewer;

@SuppressWarnings("serial")
public class BranchPatternEditSupport implements EditSupport {

	@Override
	public BeanContext<?> getBeanEditContext(Class<?> beanClass, Set<String> excludeProperties) {
		return null;
	}

	@Override
	public PropertyContext<?> getPropertyEditContext(Class<?> beanClass, String propertyName) {
		PropertyDescriptor propertyDescriptor = new PropertyDescriptor(beanClass, propertyName);
		Method propertyGetter = propertyDescriptor.getPropertyGetter();
        if (propertyGetter.getAnnotation(BranchPattern.class) != null) {
        	if (propertyGetter.getReturnType() != String.class) {
	    		throw new RuntimeException("Annotation 'BranchPattern' should be applied to property "
	    				+ "with type 'String'.");
        	}
    		return new PropertyContext<String>(propertyDescriptor) {

				@Override
				public PropertyViewer renderForView(String componentId, final IModel<String> model) {
					return new PropertyViewer(componentId, this) {

						@Override
						protected Component newContent(String id, PropertyDescriptor propertyDescriptor) {
					        String refMatch = model.getObject();
					        if (refMatch != null) {
					        	return new Label(id, refMatch);
					        } else {
								return new NotDefinedLabel(id);
					        }
						}
						
					};
				}

				@Override
				public PropertyEditor<String> renderForEdit(String componentId, IModel<String> model) {
		        	return new BranchPatternEditor(componentId, this, model);
				}
    			
    		};
        } else {
            return null;
        }
	}

}
