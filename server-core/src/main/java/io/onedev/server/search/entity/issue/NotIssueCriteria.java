package io.onedev.server.search.entity.issue;

import java.util.Collection;
import java.util.Map;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import io.onedev.server.model.Issue;
import io.onedev.server.search.entity.NotEntityCriteria;
import io.onedev.server.web.component.issue.workflowreconcile.UndefinedFieldResolution;
import io.onedev.server.web.component.issue.workflowreconcile.UndefinedFieldValue;
import io.onedev.server.web.component.issue.workflowreconcile.UndefinedFieldValuesResolution;
import io.onedev.server.web.component.issue.workflowreconcile.UndefinedStateResolution;

public class NotIssueCriteria extends IssueCriteria {
	
	private static final long serialVersionUID = 1L;

	private final IssueCriteria criteria;
	
	public NotIssueCriteria(IssueCriteria criteria) {
		this.criteria = criteria;
	}

	@Override
	public Predicate getPredicate(Root<Issue> root, CriteriaBuilder builder) {
		return new NotEntityCriteria<Issue>(criteria).getPredicate(root, builder);
	}

	@Override
	public boolean matches(Issue issue) {
		return new NotEntityCriteria<Issue>(criteria).matches(issue);
	}

	@Override
	public String toStringWithoutParens() {
		return new NotEntityCriteria<Issue>(criteria).toStringWithoutParens();
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
	
}
