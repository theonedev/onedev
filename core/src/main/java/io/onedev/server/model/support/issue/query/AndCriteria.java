package io.onedev.server.model.support.issue.query;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.criteria.Predicate;

import io.onedev.server.model.Issue;
import io.onedev.server.model.Project;

public class AndCriteria extends IssueCriteria {
	
	private static final long serialVersionUID = 1L;

	private final List<IssueCriteria> criterias;
	
	public AndCriteria(List<IssueCriteria> criterias) {
		this.criterias = criterias;
	}

	@Override
	public Predicate getPredicate(QueryBuildContext context) {
		List<Predicate> predicates = new ArrayList<>();
		for (IssueCriteria criteria: criterias)
			predicates.add(criteria.getPredicate(context));
		return context.getBuilder().and(predicates.toArray(new Predicate[0]));
	}

	@Override
	public boolean matches(Issue issue) {
		for (IssueCriteria criteria: criterias) {
			if (!criteria.matches(issue))
				return false;
		}
		return true;
	}

	@Override
	public boolean needsLogin() {
		for (IssueCriteria criteria: criterias) {
			if (criteria.needsLogin())
				return true;
		}
		return false;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		for (int i=0; i<criterias.size(); i++) {
			IssueCriteria criteria = criterias.get(i);
			if (i != 0)
				builder.append(" and ");
			if (criteria instanceof AndCriteria || criteria instanceof OrCriteria) {
				builder.append("(");
				builder.append(criteria.toString());
				builder.append(")");
			} else {
				builder.append(criteria.toString());
			}
		}
		return builder.toString().trim();
	}

	@Override
	public Collection<String> getUndefinedStates(Project project) {
		List<String> undefinedStates = new ArrayList<>();
		for (IssueCriteria criteria: criterias)
			undefinedStates.addAll(criteria.getUndefinedStates(project));
		return undefinedStates;
	}

	@Override
	public void onRenameState(String oldState, String newState) {
		for (IssueCriteria criteria: criterias)
			criteria.onRenameState(oldState, newState);
	}

	@Override
	public Collection<String> getUndefinedFields(Project project) {
		Set<String> undefinedFields = new HashSet<>();
		for (IssueCriteria criteria: criterias)
			undefinedFields.addAll(criteria.getUndefinedFields(project));
		return undefinedFields;
	}

	@Override
	public void onRenameField(String oldField, String newField) {
		for (IssueCriteria criteria: criterias)
			criteria.onRenameField(oldField, newField);
	}

	@Override
	public boolean onDeleteField(String fieldName) {
		for (Iterator<IssueCriteria> it = criterias.iterator(); it.hasNext();) {
			if (it.next().onDeleteField(fieldName))
				it.remove();
		}
		return criterias.isEmpty();
	}

	@Override
	public Map<String, String> getUndefinedFieldValues(Project project) {
		Map<String, String> undefinedFieldValues = new HashMap<>();
		for (IssueCriteria criteria: criterias)
			undefinedFieldValues.putAll(criteria.getUndefinedFieldValues(project));
		return undefinedFieldValues;
	}

	@Override
	public void onRenameFieldValue(String fieldName, String oldValue, String newValue) {
		for (IssueCriteria criteria: criterias)
			criteria.onRenameFieldValue(fieldName, oldValue, newValue);
	}

	@Override
	public boolean onDeleteFieldValue(String fieldName, String fieldValue) {
		for (Iterator<IssueCriteria> it = criterias.iterator(); it.hasNext();) {
			if (it.next().onDeleteFieldValue(fieldName, fieldValue))
				it.remove();
		}
		return criterias.isEmpty();
	}
	
}
