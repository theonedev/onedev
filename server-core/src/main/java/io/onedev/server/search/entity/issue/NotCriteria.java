package io.onedev.server.search.entity.issue;

import java.util.Collection;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import io.onedev.server.model.Issue;
import io.onedev.server.model.Project;
import io.onedev.server.model.User;
import io.onedev.server.search.entity.NotCriteriaHelper;
import io.onedev.server.util.ValueSetEdit;
import io.onedev.server.web.component.issue.workflowreconcile.UndefinedFieldValue;

public class NotCriteria extends IssueCriteria {
	
	private static final long serialVersionUID = 1L;

	private final IssueCriteria criteria;
	
	public NotCriteria(IssueCriteria criteria) {
		this.criteria = criteria;
	}

	@Override
	public Predicate getPredicate(Project project, Root<Issue> root, CriteriaBuilder builder, User user) {
		return new NotCriteriaHelper<Issue>(criteria).getPredicate(project, root, builder, user);
	}

	@Override
	public boolean matches(Issue issue, User user) {
		return new NotCriteriaHelper<Issue>(criteria).matches(issue, user);
	}

	@Override
	public boolean needsLogin() {
		return new NotCriteriaHelper<Issue>(criteria).needsLogin();
	}

	@Override
	public String toString() {
		return new NotCriteriaHelper<Issue>(criteria).toString();
	}
	
	@Override
	public void onRenameField(String oldName, String newName) {
		criteria.onRenameField(oldName, newName);
	}

	@Override
	public boolean onDeleteField(String fieldName) {
		return criteria.onDeleteField(fieldName);
	}
	
	@Override
	public void onRenameState(String oldName, String newName) {
		criteria.onRenameState(oldName, newName);
	}

	@Override
	public boolean onDeleteState(String stateName) {
		return criteria.onDeleteState(stateName);
	}

	@Override
	public Collection<String> getUndefinedStates() {
		return criteria.getUndefinedStates();
	}

	@Override
	public Collection<String> getUndefinedFields() {
		return criteria.getUndefinedFields();
	}

	@Override
	public Collection<UndefinedFieldValue> getUndefinedFieldValues() {
		return criteria.getUndefinedFieldValues();
	}

	@Override
	public boolean onEditFieldValues(String fieldName, ValueSetEdit valueSetEdit) {
		return criteria.onEditFieldValues(fieldName, valueSetEdit);
	}
	
}
