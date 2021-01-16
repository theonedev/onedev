package io.onedev.server.search.entity.issue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import io.onedev.server.model.Issue;
import io.onedev.server.search.entity.AndEntityCriteria;
import io.onedev.server.web.component.issue.workflowreconcile.UndefinedFieldResolution;
import io.onedev.server.web.component.issue.workflowreconcile.UndefinedFieldValue;
import io.onedev.server.web.component.issue.workflowreconcile.UndefinedFieldValuesResolution;
import io.onedev.server.web.component.issue.workflowreconcile.UndefinedStateResolution;

public class AndIssueCriteria extends IssueCriteria {
	
	private static final long serialVersionUID = 1L;

	private final List<IssueCriteria> criterias;
	
	public AndIssueCriteria(List<IssueCriteria> criterias) {
		this.criterias = criterias;
	}

	@Override
	public Predicate getPredicate(Root<Issue> root, CriteriaBuilder builder) {
		return new AndEntityCriteria<Issue>(criterias).getPredicate(root, builder);
	}

	@Override
	public boolean matches(Issue issue) {
		return new AndEntityCriteria<Issue>(criterias).matches(issue);
	}

	@Override
	public String toStringWithoutParens() {
		return new AndEntityCriteria<Issue>(criterias).toStringWithoutParens();
	}

	@Override
	public Collection<String> getUndefinedStates() {
		List<String> undefinedStates = new ArrayList<>();
		for (IssueCriteria criteria: criterias)
			undefinedStates.addAll(criteria.getUndefinedStates());
		return undefinedStates;
	}

	@Override
	public Collection<String> getUndefinedFields() {
		Set<String> undefinedFields = new HashSet<>();
		for (IssueCriteria criteria: criterias)
			undefinedFields.addAll(criteria.getUndefinedFields());
		return undefinedFields;
	}

	@Override
	public Collection<UndefinedFieldValue> getUndefinedFieldValues() {
		Set<UndefinedFieldValue> undefinedFieldValues = new HashSet<>();
		for (IssueCriteria criteria: criterias)
			undefinedFieldValues.addAll(criteria.getUndefinedFieldValues());
		return undefinedFieldValues;
	}

	@Override
	public boolean fixUndefinedStates(Map<String, UndefinedStateResolution> resolutions) {
		for (Iterator<IssueCriteria> it = criterias.iterator(); it.hasNext();) {
			if (!it.next().fixUndefinedStates(resolutions))
				it.remove();
		}
		return !criterias.isEmpty();
	}
	
	@Override
	public boolean fixUndefinedFields(Map<String, UndefinedFieldResolution> resolutions) {
		for (Iterator<IssueCriteria> it = criterias.iterator(); it.hasNext();) {
			if (!it.next().fixUndefinedFields(resolutions))
				it.remove();
		}
		return !criterias.isEmpty();
	}
	
	@Override
	public boolean fixUndefinedFieldValues(Map<String, UndefinedFieldValuesResolution> resolutions) {
		for (Iterator<IssueCriteria> it = criterias.iterator(); it.hasNext();) {
			if (!it.next().fixUndefinedFieldValues(resolutions))
				it.remove();
		}
		return !criterias.isEmpty();
	}
	
	@Override
	public void fill(Issue issue) {
		for (IssueCriteria criteria: criterias)
			criteria.fill(issue);
	}
	
}
