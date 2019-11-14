package io.onedev.server.search.entity.issue;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.Nullable;

import io.onedev.server.model.Issue;
import io.onedev.server.search.entity.EntityCriteria;
import io.onedev.server.util.ValueSetEdit;
import io.onedev.server.web.component.issue.workflowreconcile.UndefinedFieldValue;

public abstract class IssueCriteria extends EntityCriteria<Issue> {
	
	private static final long serialVersionUID = 1L;
	
	public final void fill(Issue issue) {
		fill(issue, new HashSet<>());
	}
	
	public void fill(Issue issue, Set<String> initedLists) {
	}
	
	public Collection<String> getUndefinedStates() {
		return new HashSet<>();
	}

	public void onRenameState(String oldState, String newState) {
	}
	
	public Collection<String> getUndefinedFields() {
		return new HashSet<>();
	}
	
	public void onRenameField(String oldField, String newField) {
	}
	
	public boolean onDeleteField(String fieldName) {
		return false;
	}
	
	public boolean onDeleteState(String stateName) {
		return false;
	}
	
	public Collection<UndefinedFieldValue> getUndefinedFieldValues() {
		return new HashSet<>();
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

	public boolean onEditFieldValues(String fieldName, ValueSetEdit valueSetEdit) {
		return false;
	}
	
}
