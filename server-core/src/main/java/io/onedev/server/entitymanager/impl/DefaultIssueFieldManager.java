package io.onedev.server.entitymanager.impl;

import com.google.common.base.Preconditions;
import io.onedev.server.entitymanager.IssueFieldManager;
import io.onedev.server.model.Issue;
import io.onedev.server.model.IssueField;
import io.onedev.server.model.support.issue.field.spec.FieldSpec;
import io.onedev.server.persistence.annotation.Sessional;
import io.onedev.server.persistence.annotation.Transactional;
import io.onedev.server.persistence.dao.BaseEntityManager;
import io.onedev.server.persistence.dao.Dao;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.persistence.Query;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

@Singleton
public class DefaultIssueFieldManager extends BaseEntityManager<IssueField> implements IssueFieldManager {
	
	@Inject
	public DefaultIssueFieldManager(Dao dao) {
		super(dao);
	}

	@Transactional
	@Override
	public void saveFields(Issue issue) {
		Collection<Long> ids = new HashSet<>();
		for (IssueField field: issue.getFields()) {
			if (field.getId() != null)
				ids.add(field.getId());
		}
		if (!ids.isEmpty()) {
			Query query = getSession().createQuery("delete from IssueField where issue = :issue and id not in (:ids)");
			query.setParameter("issue", issue);
			query.setParameter("ids", ids);
			query.executeUpdate();
		} else {
			Query query = getSession().createQuery("delete from IssueField where issue = :issue");
			query.setParameter("issue", issue);
			query.executeUpdate();
		}
		
		for (IssueField field: issue.getFields()) {
			if (field.isNew())
				create(field);
		}
	}

	@Transactional
	@Override
	public void create(IssueField field) {
		Preconditions.checkState(field.isNew());
		dao.persist(field);
	}

	@Transactional
	@Override
	public void onRenameGroup(String oldName, String newName) {
		Query query = getSession().createQuery("update IssueField set value=:newName where type=:type and value=:oldName");
		query.setParameter("type", FieldSpec.GROUP);
		query.setParameter("oldName", oldName);
		query.setParameter("newName", newName);
		query.executeUpdate();
	}

	@Transactional
	@Override
	public void onRenameUser(String oldName, String newName) {
		Query query = getSession().createQuery("update IssueField set value=:newName where type=:type and value=:oldName");
		query.setParameter("type", FieldSpec.USER);
		query.setParameter("oldName", oldName);
		query.setParameter("newName", newName);
		query.executeUpdate();
	}

	@Sessional
	@Override
	public void populateFields(Collection<Issue> issues) {
		CriteriaBuilder builder = getSession().getCriteriaBuilder();
		CriteriaQuery<IssueField> query = builder.createQuery(IssueField.class);
		
		Root<IssueField> root = query.from(IssueField.class);
		query.select(root);
		
		query.where(root.get(IssueField.PROP_ISSUE).in(issues));
		
		for (Issue issue: issues)
			issue.setFields(new ArrayList<>());
		
		for (IssueField field: getSession().createQuery(query).getResultList())
			field.getIssue().getFields().add(field);
	}
	
}
