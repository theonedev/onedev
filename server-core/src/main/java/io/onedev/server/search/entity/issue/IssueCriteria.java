package io.onedev.server.search.entity.issue;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import io.onedev.server.model.Issue;
import io.onedev.server.search.entity.EntityCriteria;
import io.onedev.server.web.component.issue.workflowreconcile.UndefinedFieldResolution;
import io.onedev.server.web.component.issue.workflowreconcile.UndefinedFieldValue;
import io.onedev.server.web.component.issue.workflowreconcile.UndefinedFieldValuesResolution;
import io.onedev.server.web.component.issue.workflowreconcile.UndefinedStateResolution;

public abstract class IssueCriteria extends EntityCriteria<Issue> {
	
	private static final long serialVersionUID = 1L;
	
	public void fill(Issue issue) {
	}
	
	public Collection<String> getUndefinedStates() {
		return new HashSet<>();
	}

	public Collection<String> getUndefinedFields() {
		return new HashSet<>();
	}
	
	public Collection<UndefinedFieldValue> getUndefinedFieldValues() {
		return new HashSet<>();
	}

	public boolean fixUndefinedStates(Map<String, UndefinedStateResolution> resolutions) {
		return true;
	}
	
	public boolean fixUndefinedFields(Map<String, UndefinedFieldResolution> resolutions) {
		return true;
	}
	
	public boolean fixUndefinedFieldValues(Map<String, UndefinedFieldValuesResolution> resolutions) {
		return true;
	}
	
	@Nullable
	public static IssueCriteria and(List<IssueCriteria> criterias) {
		if (criterias.size() > 1)
			return new AndIssueCriteria(criterias);
		else if (criterias.size() == 1)
			return criterias.iterator().next();
		else
			return null;
	}

	@Nullable
	public static IssueCriteria or(List<IssueCriteria> criterias) {
		if (criterias.size() > 1)
			return new OrIssueCriteria(criterias);
		else if (criterias.size() == 1)
			return criterias.iterator().next();
		else
			return null;
	}
	
}
