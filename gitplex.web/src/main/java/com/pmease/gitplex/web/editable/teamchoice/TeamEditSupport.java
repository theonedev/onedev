package com.pmease.gitplex.web.editable.teamchoice;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.IModel;
import org.eclipse.jgit.util.StringUtils;

import com.pmease.commons.hibernate.dao.Dao;
import com.pmease.commons.wicket.editable.BeanContext;
import com.pmease.commons.wicket.editable.DefaultPropertyDescriptor;
import com.pmease.commons.wicket.editable.EditSupport;
import com.pmease.commons.wicket.editable.EditableUtils;
import com.pmease.commons.wicket.editable.NotDefinedLabel;
import com.pmease.commons.wicket.editable.PropertyContext;
import com.pmease.commons.wicket.editable.PropertyDescriptor;
import com.pmease.commons.wicket.editable.PropertyEditor;
import com.pmease.commons.wicket.editable.PropertyViewer;
import com.pmease.gitplex.core.GitPlex;
import com.pmease.gitplex.core.annotation.TeamChoice;
import com.pmease.gitplex.core.model.Team;

@SuppressWarnings("serial")
public class TeamEditSupport implements EditSupport {

	@Override
	public BeanContext<?> getBeanEditContext(Class<?> beanClass) {
		return null;
	}

	@Override
	public PropertyContext<?> getPropertyEditContext(Class<?> beanClass, String propertyName) {
		PropertyDescriptor propertyDescriptor = new DefaultPropertyDescriptor(beanClass, propertyName);
		Method propertyGetter = propertyDescriptor.getPropertyGetter();
        if (propertyGetter.getAnnotation(TeamChoice.class) != null) {
        	if (List.class.isAssignableFrom(propertyGetter.getReturnType()) 
        			&& EditableUtils.getElementClass(propertyGetter.getGenericReturnType()) == Long.class) {
        		return new PropertyContext<List<Long>>(propertyDescriptor) {

					@Override
					public PropertyViewer renderForView(String componentId, final IModel<List<Long>> model) {
						return new PropertyViewer(componentId, this) {

							@Override
							protected Component newContent(String id, PropertyDescriptor propertyDescriptor) {
						        List<Long> teamIds = model.getObject();
						        if (teamIds != null && !teamIds.isEmpty()) {
						        	Dao dao = GitPlex.getInstance(Dao.class);
						        	List<String> teamNames = new ArrayList<>();
						        	for (Long teamId: teamIds) {
						        		teamNames.add(dao.load(Team.class, teamId).getName());
						        	}
						            return new Label(id, StringUtils.join(teamNames, ", " ));
						        } else {
									return new NotDefinedLabel(id);
						        }
							}
							
						};
					}

					@Override
					public PropertyEditor<List<Long>> renderForEdit(String componentId, IModel<List<Long>> model) {
						return new TeamMultiChoiceEditor(componentId, this, model);
					}
        			
        		};
        	} else if (propertyGetter.getReturnType() == Long.class) {
        		return new PropertyContext<Long>(propertyDescriptor) {

					@Override
					public PropertyViewer renderForView(String componentId, final IModel<Long> model) {
						return new PropertyViewer(componentId, this) {

							@Override
							protected Component newContent(String id, PropertyDescriptor propertyDescriptor) {
						        Long teamId = model.getObject();
						        if (teamId != null) {
						        	Team team = GitPlex.getInstance(Dao.class).load(Team.class, teamId);
						            return new Label(id, team.getName());
						        } else {
									return new NotDefinedLabel(id);
						        }
							}
							
						};
					}

					@Override
					public PropertyEditor<Long> renderForEdit(String componentId, IModel<Long> model) {
						return new TeamSingleChoiceEditor(componentId, this, model);
					}
        			
        		};
        	} else {
        		throw new RuntimeException("Annotation 'TeamChoice' should be applied to property with type Long or List<Long>.");
        	}
        } else {
            return null;
        }
	}

}
