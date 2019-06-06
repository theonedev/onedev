package io.onedev.server.search.entity.issue;

import java.util.Set;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;

import io.onedev.server.model.Issue;
import io.onedev.server.model.IssueField;
import io.onedev.server.model.Project;
import io.onedev.server.model.User;

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
	protected Predicate getValuePredicate(Project project, Join<?, ?> field, CriteriaBuilder builder, User user) {
		Path<String> attribute = field.get(IssueField.ATTR_VALUE);
		if (operator == IssueQueryLexer.Is)
			return builder.equal(builder.lower(attribute), value.toLowerCase());
		else 
			return builder.like(builder.lower(attribute), "%" + value.toLowerCase() + "%");
	}

	@Override
	public boolean matches(Issue issue, User user) {
		String fieldValue = (String) issue.getFieldValue(getFieldName());
		if (operator == IssueQueryLexer.Is)
			return value.equalsIgnoreCase(fieldValue);
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
		if (operator == IssueQueryLexer.Is)
			issue.setFieldValue(getFieldName(), value);
	}
	
}
