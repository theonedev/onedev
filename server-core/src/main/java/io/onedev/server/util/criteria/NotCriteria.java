package io.onedev.server.util.criteria;

import java.util.Collection;
import java.util.Map;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Predicate;

import io.onedev.server.web.component.issue.workflowreconcile.UndefinedFieldResolution;
import io.onedev.server.web.component.issue.workflowreconcile.UndefinedFieldValue;
import io.onedev.server.web.component.issue.workflowreconcile.UndefinedFieldValuesResolution;
import io.onedev.server.web.component.issue.workflowreconcile.UndefinedStateResolution;

public class NotCriteria<T> extends Criteria<T> {
	
	private static final long serialVersionUID = 1L;

	protected final Criteria<T> criteria;
	
	public NotCriteria(Criteria<T> criteria) {
		this.criteria = criteria;
	}

	@Override
	public Predicate getPredicate(CriteriaQuery<?> query, From<T, T> root, CriteriaBuilder builder) {
		return builder.not(criteria.getPredicate(query, root, builder));
	}
	
	@Override
	public boolean matches(T entity) {
		return !criteria.matches(entity);
	}

	@Override
	public void onRenameUser(String oldName, String newName) {
		criteria.onRenameUser(oldName, newName);
	}

	@Override
	public void onRenameRole(String oldName, String newName) {
		criteria.onRenameRole(oldName, newName);
	}
	
	@Override
	public void onMoveProject(String oldPath, String newPath) {
		criteria.onMoveProject(oldPath, newPath);
	}

	@Override
	public void onRenameGroup(String oldName, String newName) {
		criteria.onRenameGroup(oldName, newName);
	}

	@Override
	public void onRenameLink(String oldPath, String newPath) {
		criteria.onRenameLink(oldPath, newPath);
	}
	
	@Override
	public boolean isUsingUser(String userName) {
		return criteria.isUsingUser(userName);
	}

	@Override
	public boolean isUsingRole(String roleName) {
		return criteria.isUsingRole(roleName);
	}
	
	@Override
	public boolean isUsingProject(String projectPath) {
		return criteria.isUsingProject(projectPath);
	}

	@Override
	public boolean isUsingGroup(String groupName) {
		return criteria.isUsingGroup(groupName);
	}
	
	@Override
	public boolean isUsingLink(String linkName) {
		return criteria.isUsingLink(linkName);
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
	public boolean fixUndefinedStates(Map<String, UndefinedStateResolution> resolutions) {
		return criteria.fixUndefinedStates(resolutions);
	}

	@Override
	public boolean fixUndefinedFields(Map<String, UndefinedFieldResolution> resolutions) {
		return criteria.fixUndefinedFields(resolutions);
	}

	@Override
	public boolean fixUndefinedFieldValues(Map<String, UndefinedFieldValuesResolution> resolutions) {
		return criteria.fixUndefinedFieldValues(resolutions);
	}
	
	@Override
	public String toStringWithoutParens() {
		return "not(" + criteria.toString() + ")";
	}
	
}
