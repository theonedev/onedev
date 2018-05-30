package io.onedev.server.model.support.issue.query;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.persistence.criteria.Join;
import javax.persistence.criteria.Predicate;

import io.onedev.server.model.Issue;
import io.onedev.server.model.IssueFieldUnary;
import io.onedev.server.model.Project;
import io.onedev.server.util.inputspec.InputSpec;
import io.onedev.server.util.inputspec.choiceinput.ChoiceInput;

public class ChoiceFieldCriteria extends FieldCriteria {

	private static final long serialVersionUID = 1L;

	private String value;
	
	private final long ordinal;
	
	private final int operator;
	
	private final boolean allowMultiple;
	
	public ChoiceFieldCriteria(String name, String value, long ordinal, int operator, boolean allowMultiple) {
		super(name);
		this.value = value;
		this.ordinal = ordinal;
		this.operator = operator;
		this.allowMultiple = allowMultiple;
	}

	@Override
	public Predicate getPredicate(QueryBuildContext context) {
		Join<Issue, ?> join = context.getJoin(getFieldName());
		if (allowMultiple) {
			if (operator == IssueQueryLexer.Contains)
				return context.getBuilder().equal(join.get(IssueFieldUnary.VALUE), value);
			else
				return context.getBuilder().notEqual(join.get(IssueFieldUnary.VALUE), value);
		} else {
			if (operator == IssueQueryLexer.Is)
				return context.getBuilder().equal(join.get(IssueFieldUnary.VALUE), value);
			else if (operator == IssueQueryLexer.IsNot)
				return context.getBuilder().notEqual(join.get(IssueFieldUnary.VALUE), value);
			else if (operator == IssueQueryLexer.IsGreaterThan)
				return context.getBuilder().greaterThan(join.get(IssueFieldUnary.ORDINAL), ordinal);
			else
				return context.getBuilder().lessThan(join.get(IssueFieldUnary.ORDINAL), ordinal);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean matches(Issue issue) {
		if (allowMultiple) {
			List<String> fieldValue = (List<String>) getFieldValue(issue);
			if (operator == IssueQueryLexer.Contains)
				return fieldValue.contains(value);
			else
				return !fieldValue.contains(value);
		} else {
			Object fieldValue = getFieldValue(issue);
			if (operator == IssueQueryLexer.Is)
				return Objects.equals(fieldValue, value);
			else if (operator == IssueQueryLexer.IsNot)
				return !Objects.equals(fieldValue, value);
			else if (operator == IssueQueryLexer.IsGreaterThan)
				return getFieldOrdinal(issue) > ordinal;
			else
				return getFieldOrdinal(issue) < ordinal;
		}
	}

	@Override
	public boolean needsLogin() {
		return false;
	}

	@Override
	public String toString() {
		return quote(getFieldName()) + " " + IssueQuery.getOperatorName(operator) + " " + quote(value);
	}

	@Override
	public Map<String, String> getUndefinedFieldValues(Project project) {
		Map<String, String> undefinedFieldValues = new HashMap<>();
		InputSpec fieldSpec = project.getIssueWorkflow().getFieldSpec(getFieldName());
		List<String> choices = new ArrayList<>(((ChoiceInput)fieldSpec).getChoiceProvider().getChoices(true).keySet());
		if (!choices.contains(value))
			undefinedFieldValues.put(getFieldName(), value);
		return undefinedFieldValues;
	}
	
	@Override
	public void onRenameFieldValue(String fieldName, String oldValue, String newValue) {
		if (fieldName.equals(getFieldName()) && oldValue.equals(value))
			value = newValue;
	}

	@Override
	public boolean onDeleteFieldValue(String fieldName, String fieldValue) {
		return fieldName.equals(getFieldName()) && fieldValue.equals(value);
	}

}
