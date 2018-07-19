package io.onedev.server.model.support.issue.query;

import java.util.Objects;
import java.util.Set;

import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;

import io.onedev.server.model.Issue;
import io.onedev.server.model.IssueFieldUnary;
import io.onedev.server.model.Project;
import io.onedev.server.util.query.QueryBuildContext;

public class StringFieldCriteria extends FieldCriteria {

	private static final long serialVersionUID = 1L;

	private final String value;
	
	private final int operator;
	
	public StringFieldCriteria(String name, String value, int operator) {
		super(name);
		this.value = value;
		this.operator = operator;
	}

	public String getValue() {
		return value;
	}

	@Override
	public Predicate getPredicate(Project project, QueryBuildContext<Issue> context) {
		Path<String> attribute = context.getJoin(getFieldName()).get(IssueFieldUnary.VALUE);
		if (operator == IssueQueryLexer.Is)
			return context.getBuilder().equal(attribute, value);
		else 
			return context.getBuilder().like(attribute, "%" + value + "%");
	}

	@Override
	public boolean matches(Issue issue) {
		String fieldValue = (String) issue.getFieldValue(getFieldName());
		if (operator == IssueQueryLexer.Is)
			return Objects.equals(fieldValue, value);
		else 
			return fieldValue != null && fieldValue.toLowerCase().contains(value.toLowerCase());
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
	public void fill(Issue issue, Set<String> initedLists) {
		issue.setFieldValue(getFieldName(), value);
	}
	
}
