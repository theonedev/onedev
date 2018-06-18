package io.onedev.server.model.support.issue.query;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import javax.persistence.criteria.Join;
import javax.persistence.criteria.Predicate;

import io.onedev.server.model.Issue;
import io.onedev.server.model.IssueFieldUnary;
import io.onedev.server.model.Project;
import io.onedev.server.util.inputspec.InputSpec;
import io.onedev.server.web.page.project.issues.workflowreconcile.UndefinedFieldValue;

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
	public Predicate getPredicate(Project project, QueryBuildContext context) {
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
		return IssueQuery.quote(getFieldName()) + " " + IssueQuery.getRuleName(operator) + " " + IssueQuery.quote(value);
	}

	@Override
	public Collection<UndefinedFieldValue> getUndefinedFieldValues(Project project) {
		Set<UndefinedFieldValue> undefinedFieldValues = new HashSet<>();
		InputSpec fieldSpec = project.getIssueWorkflow().getFieldSpec(getFieldName());
		List<String> choices = fieldSpec.getPossibleValues();
		if (!choices.contains(value))
			undefinedFieldValues.add(new UndefinedFieldValue(getFieldName(), value));
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

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public void populate(Issue issue, Serializable fieldBean, Set<String> initedLists) {
		if (allowMultiple) {
			if (operator == IssueQueryLexer.Contains) {
				List list;
				if (!initedLists.contains(getFieldName())) {
					list = new ArrayList();
					setFieldValue(fieldBean, list);
					initedLists.add(getFieldName());
				} else {
					list = (List) getFieldValue(fieldBean);
					if (list == null) {
						list = new ArrayList();
						setFieldValue(fieldBean, list);
					}
				}
				list.add(value);
			}
		} else if (operator == IssueQueryLexer.Is) {
			setFieldValue(fieldBean, value);
		}
	}

}
