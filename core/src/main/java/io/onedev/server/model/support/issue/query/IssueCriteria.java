package io.onedev.server.model.support.issue.query;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.Nullable;
import javax.persistence.criteria.Predicate;

import io.onedev.server.model.Issue;
import io.onedev.server.model.Project;
import io.onedev.server.web.page.project.issues.workflowreconcile.UndefinedFieldValue;

public abstract class IssueCriteria implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	public abstract Predicate getPredicate(Project project, QueryBuildContext context);

	public final void fill(Issue issue) {
		fill(issue, new HashSet<>());
	}
	
	public void fill(Issue issue, Set<String> initedLists) {
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

	public Collection<UndefinedFieldValue> getUndefinedFieldValues(Project project) {
		return new HashSet<>();
	}

	public void onRenameFieldValue(String fieldName, String oldValue, String newValue) {
	}

	public boolean onDeleteFieldValue(String fieldName, String fieldValue) {
		return false;
	}
	
	@Nullable
	public static IssueCriteria of(List<IssueCriteria> criterias) {
		if (criterias.size() > 1)
			return new AndCriteria(criterias);
		else if (criterias.size() == 1)
			return criterias.iterator().next();
		else
			return null;
	}
}
