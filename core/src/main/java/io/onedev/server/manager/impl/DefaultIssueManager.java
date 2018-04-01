package io.onedev.server.manager.impl;

import java.io.Serializable;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.onedev.server.manager.IssueManager;
import io.onedev.server.manager.ProjectManager;
import io.onedev.server.model.Issue;
import io.onedev.server.model.Project;
import io.onedev.server.model.User;
import io.onedev.server.persistence.annotation.Sessional;
import io.onedev.server.persistence.dao.AbstractEntityManager;
import io.onedev.server.persistence.dao.Dao;
import io.onedev.server.util.inputspec.InputSpec;

@Singleton
public class DefaultIssueManager extends AbstractEntityManager<Issue> 
		implements IssueManager {

	private static final String CUSTOM_FIELDS_BEAN_PREFIX = "IssueFieldsBean";
	
	private final ProjectManager projectManager;
	
	@Inject
	public DefaultIssueManager(Dao dao, ProjectManager projectManager) {
		super(dao);
		this.projectManager = projectManager;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Class<? extends Serializable> defineCustomFieldsBeanClass(Project project) {
		String className = CUSTOM_FIELDS_BEAN_PREFIX + project.getId();
		
		return (Class<? extends Serializable>) InputSpec.defineClass(className, project.getIssueWorkflow().getFields());
	}
	
	@Override
	public Class<? extends Serializable> loadCustomFieldsBeanClass(String className) {
		if (className.startsWith(CUSTOM_FIELDS_BEAN_PREFIX)) {
			Long projectId = Long.valueOf(className.substring(CUSTOM_FIELDS_BEAN_PREFIX.length()));
			return defineCustomFieldsBeanClass(projectManager.load(projectId));
		} else {
			return null;
		}
	}

	@Sessional
	@Override
	public List<Issue> query(Project project, User user) {
		//Query<Issue> query = getSession().createQuery("select distinct issue from Issue as issue left join issue.fields as field with field.type='user' and field.value='admin' where issue.project.id=1");
		//query.setFirstResult(1);
		//query.setMaxResults(1);
		//return query.list();
		return null;
	}

}
