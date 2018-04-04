package io.onedev.server.manager.impl;

import java.io.Serializable;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.onedev.server.manager.IssueFieldManager;
import io.onedev.server.manager.IssueManager;
import io.onedev.server.model.Issue;
import io.onedev.server.model.Project;
import io.onedev.server.model.User;
import io.onedev.server.persistence.annotation.Sessional;
import io.onedev.server.persistence.annotation.Transactional;
import io.onedev.server.persistence.dao.AbstractEntityManager;
import io.onedev.server.persistence.dao.Dao;

@Singleton
public class DefaultIssueManager extends AbstractEntityManager<Issue> implements IssueManager {

	private final IssueFieldManager issueFieldManager;
	
	@Inject
	public DefaultIssueManager(Dao dao, IssueFieldManager issueFieldManager) {
		super(dao);
		this.issueFieldManager = issueFieldManager;
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

	@Transactional
	@Override
	public void save(Issue issue, Serializable fieldBean) {
		save(issue);
		issueFieldManager.saveFields(issue, fieldBean);
	}

}
