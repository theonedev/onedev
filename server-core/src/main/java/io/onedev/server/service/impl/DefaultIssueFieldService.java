package io.onedev.server.service.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

import jakarta.inject.Singleton;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;

import com.google.common.base.Preconditions;

import io.onedev.server.model.Issue;
import io.onedev.server.model.IssueField;
import io.onedev.server.model.support.issue.field.spec.FieldSpec;
import io.onedev.server.persistence.annotation.Sessional;
import io.onedev.server.persistence.annotation.Transactional;
import io.onedev.server.service.IssueFieldService;

@Singleton
public class DefaultIssueFieldService extends BaseEntityService<IssueField> implements IssueFieldService {

	@Transactional
	@Override
	public void saveFields(Issue issue) {
		Collection<Long> ids = new HashSet<>();
		for (IssueField field: issue.getFields()) {
			if (field.getId() != null)
				ids.add(field.getId());
		}
		if (!ids.isEmpty()) {
			var query = getSession().createMutationQuery("delete from IssueField where issue = :issue and id not in (:ids)");
			query.setParameter("issue", issue);
			query.setParameter("ids", ids);
			query.executeUpdate();
		} else {
			var query = getSession().createMutationQuery("delete from IssueField where issue = :issue");
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
		var query = getSession().createMutationQuery("update IssueField set value=:newName where type=:type and value=:oldName");
		query.setParameter("type", FieldSpec.GROUP);
		query.setParameter("oldName", oldName);
		query.setParameter("newName", newName);
		query.executeUpdate();
	}

	@Transactional
	@Override
	public void onRenameUser(String oldName, String newName) {
		var query = getSession().createMutationQuery("update IssueField set value=:newName where type=:type and value=:oldName");
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
