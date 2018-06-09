package io.onedev.server.model.support.issue.query;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.persistence.criteria.Predicate;

import io.onedev.server.model.Issue;
import io.onedev.server.model.Project;

public abstract class IssueCriteria implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	public abstract Predicate getPredicate(QueryBuildContext context);

	public final void populate(Issue issue, Serializable fieldBean) {
		populate(issue, fieldBean, new HashSet<>());
	}
	
	public void populate(Issue issue, Serializable fieldBean, Set<String> initedLists) {
	}
	
	public abstract boolean matches(Issue issue);
	
	public abstract boolean needsLogin();
	
	public abstract String toString();

	public Collection<String> getUndefinedStates(Project project) {
		return new HashSet<>();
	}

	public void onRenameState(String oldState, String newState) {
	}
	
	public Collection<String> getUndefinedFields(Project project) {
		return new HashSet<>();
	}
	
	public void onRenameField(String oldField, String newField) {
	}
	
	public boolean onDeleteField(String fieldName) {
		return false;
	}

	public Map<String, String> getUndefinedFieldValues(Project project) {
		return new HashMap<>();
	}

	public void onRenameFieldValue(String fieldName, String oldValue, String newValue) {
	}

	public boolean onDeleteFieldValue(String fieldName, String fieldValue) {
		return false;
	}
	
}
