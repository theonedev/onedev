package io.onedev.server.model.support.issue.query;

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
import io.onedev.server.util.query.QueryBuildContext;
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
	public Predicate getPredicate(Project project, QueryBuildContext<Issue> context) {
		Join<?, ?> join = context.getJoin(getFieldName());
		if (allowMultiple) {
			return context.getBuilder().equal(join.get(IssueFieldUnary.VALUE), value);
		} else {
			if (operator == IssueQueryLexer.Is)
				return context.getBuilder().equal(join.get(IssueFieldUnary.VALUE), value);
			else if (operator == IssueQueryLexer.IsGreaterThan)
				return context.getBuilder().greaterThan(join.get(IssueFieldUnary.ORDINAL), ordinal);
			else
				return context.getBuilder().lessThan(join.get(IssueFieldUnary.ORDINAL), ordinal);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean matches(Issue issue) {
		Object fieldValue = issue.getFieldValue(getFieldName());
		if (allowMultiple) {
			return ((List<String>)fieldValue).contains(value);
		} else {
			if (operator == IssueQueryLexer.Is)
				return Objects.equals(fieldValue, value);
			else if (operator == IssueQueryLexer.IsGreaterThan)
				return issue.getFieldOrdinal(getFieldName(), fieldValue) > ordinal;
			else
				return issue.getFieldOrdinal(getFieldName(), fieldValue) < ordinal;
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
	public void fill(Issue issue, Set<String> initedLists) {
		if (allowMultiple) {
			List list;
			if (!initedLists.contains(getFieldName())) {
				list = new ArrayList();
				issue.setFieldValue(getFieldName(), list);
				initedLists.add(getFieldName());
			} else {
				list = (List) issue.getFieldValue(getFieldName());
				if (list == null) {
					list = new ArrayList();
					issue.setFieldValue(getFieldName(), list);
				}
			}
			list.add(value);
		} else {
			issue.setFieldValue(getFieldName(), value);
		}
	}

}
