package com.gitplex.server.web.editable.teamchoice;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Set;

import org.apache.wicket.Component;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.IModel;
import org.eclipse.jgit.util.StringUtils;

import com.gitplex.server.util.editable.EditableUtils;
import com.gitplex.server.util.editable.annotation.TeamChoice;
import com.gitplex.server.web.editable.BeanContext;
import com.gitplex.server.web.editable.EditSupport;
import com.gitplex.server.web.editable.NotDefinedLabel;
import com.gitplex.server.web.editable.PropertyContext;
import com.gitplex.server.web.editable.PropertyDescriptor;
import com.gitplex.server.web.editable.PropertyEditor;
import com.gitplex.server.web.editable.PropertyViewer;

@SuppressWarnings("serial")
public class TeamEditSupport implements EditSupport {

	@Override
	public BeanContext<?> getBeanEditContext(Class<?> beanClass, Set<String> excludeProperties) {
		return null;
	}

	@Override
	public PropertyContext<?> getPropertyEditContext(Class<?> beanClass, String propertyName) {
		PropertyDescriptor propertyDescriptor = new PropertyDescriptor(beanClass, propertyName);
		Method propertyGetter = propertyDescriptor.getPropertyGetter();
        if (propertyGetter.getAnnotation(TeamChoice.class) != null) {
        	if (Collection.class.isAssignableFrom(propertyGetter.getReturnType()) 
        			&& EditableUtils.getElementClass(propertyGetter.getGenericReturnType()) == String.class) {
        		return new PropertyContext<Collection<String>>(propertyDescriptor) {

					@Override
					public PropertyViewer renderForView(String componentId, final IModel<Collection<String>> model) {
						return new PropertyViewer(componentId, this) {

							@Override
							protected Component newContent(String id, PropertyDescriptor propertyDescriptor) {
								Collection<String> teamNames = model.getObject();
						        if (teamNames != null && !teamNames.isEmpty()) {
						            return new Label(id, StringUtils.join(teamNames, ", " ));
						        } else {
									return new NotDefinedLabel(id);
						        }
							}
							
						};
					}

					@Override
					public PropertyEditor<Collection<String>> renderForEdit(String componentId, IModel<Collection<String>> model) {
						return new TeamMultiChoiceEditor(componentId, this, model);
					}
        			
        		};
        	} else if (propertyGetter.getReturnType() == String.class) {
        		return new PropertyContext<String>(propertyDescriptor) {

					@Override
					public PropertyViewer renderForView(String componentId, final IModel<String> model) {
						return new PropertyViewer(componentId, this) {

							@Override
							protected Component newContent(String id, PropertyDescriptor propertyDescriptor) {
						        String teamName = model.getObject();
						        if (teamName != null) {
						            return new Label(id, teamName);
						        } else {
									return new NotDefinedLabel(id);
						        }
							}
							
						};
					}

					@Override
					public PropertyEditor<String> renderForEdit(String componentId, IModel<String> model) {
						return new TeamSingleChoiceEditor(componentId, this, model);
					}
        			
        		};
        	} else {
        		throw new RuntimeException("Annotation 'TeamChoice' should be applied to property with type String or Collection<String>.");
        	}
        } else {
            return null;
        }
	}

}
