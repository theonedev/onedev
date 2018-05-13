package io.onedev.server.manager.impl;

import java.io.Serializable;
import java.util.Collection;
import java.util.Date;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.hibernate.criterion.Restrictions;

import io.onedev.server.manager.IssueChangeManager;
import io.onedev.server.manager.IssueFieldManager;
import io.onedev.server.manager.IssueManager;
import io.onedev.server.model.Issue;
import io.onedev.server.model.IssueChange;
import io.onedev.server.model.IssueField;
import io.onedev.server.model.support.issue.PromptedField;
import io.onedev.server.persistence.annotation.Transactional;
import io.onedev.server.persistence.dao.AbstractEntityManager;
import io.onedev.server.persistence.dao.Dao;
import io.onedev.server.persistence.dao.EntityCriteria;
import io.onedev.server.security.SecurityUtils;
import io.onedev.utils.StringUtils;

@Singleton
public class DefaultIssueChangeManager extends AbstractEntityManager<IssueChange>
		implements IssueChangeManager {

	private final IssueManager issueManager;
	
	private final IssueFieldManager issueFieldManager;
	
	@Inject
	public DefaultIssueChangeManager(Dao dao, IssueManager issueManager, IssueFieldManager issueFieldManager) {
		super(dao);
		this.issueManager = issueManager;
		this.issueFieldManager = issueFieldManager;
	}

	@Transactional
	@Override
	public void changeTitle(Issue issue, String prevTitle) {
		issueManager.save(issue);
		IssueChange edit = new IssueChange();
		edit.setIssue(issue);
		edit.setProperty("title");
		edit.setDate(new Date());
		edit.setUser(SecurityUtils.getUser());
		edit.setPrevContent(prevTitle);
		edit.setContent(issue.getTitle());
		save(edit);
	}
	
	@Transactional
	@Override
	public void changeDescription(Issue issue, String prevDescription) {
		issueManager.save(issue);
		IssueChange edit = new IssueChange();
		edit.setIssue(issue);
		edit.setProperty("description");
		edit.setDate(new Date());
		edit.setUser(SecurityUtils.getUser());
		edit.setPrevContent(prevDescription);
		edit.setContent(issue.getDescription());
		save(edit);
	}

	private String toString(Map<String, PromptedField> fields) {
		StringBuilder builder = new StringBuilder();
		for (Map.Entry<String, PromptedField> entry: fields.entrySet()) {
			builder.append(entry.getKey()).append(": ");
			builder.append(StringUtils.join(entry.getValue().getValues())).append("\n");
		}
		return builder.toString();
	}
	
	@Transactional
	@Override
	public void changeFields(Issue issue, Serializable fieldBean, Map<String, PromptedField> prevFields, 
			Collection<String> promptedFields) {
		issueFieldManager.writeFields(issue, fieldBean, promptedFields);
		IssueChange edit = new IssueChange();
		edit.setIssue(issue);
		edit.setProperty("custom fields");
		edit.setDate(new Date());
		edit.setUser(SecurityUtils.getUser());
		edit.setPrevContent(toString(prevFields));
		
		getSession().flush();
		
		EntityCriteria<IssueField> criteria = EntityCriteria.of(IssueField.class);
		criteria.add(Restrictions.eq("issue", issue));
		issue.setFields(dao.findAll(criteria));
		
		edit.setContent(toString(issue.getPromptedFields()));
		save(edit);
	}
	
}
