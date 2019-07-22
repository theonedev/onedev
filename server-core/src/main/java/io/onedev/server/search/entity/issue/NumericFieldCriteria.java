package io.onedev.server.search.entity.issue;

import java.util.Objects;
import java.util.Set;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;

import io.onedev.server.model.Issue;
import io.onedev.server.model.IssueField;
import io.onedev.server.model.Project;
import io.onedev.server.model.User;

public class NumericFieldCriteria extends FieldCriteria {

	private static final long serialVersionUID = 1L;

	private final int value;
	
	private final int operator;
	
	public NumericFieldCriteria(String name, int value, int operator) {
		super(name);
		this.value = value;
		this.operator = operator;
	}

	@Override
	protected Predicate getValuePredicate(Project project, Join<?, ?> field, CriteriaBuilder builder, User user) {
		Path<Integer> attribute = field.get(IssueField.ATTR_ORDINAL);
		if (operator == IssueQueryLexer.Is)
			return builder.equal(attribute, value);
		else if (operator == IssueQueryLexer.IsGreaterThan)
			return builder.greaterThan(attribute, value);
		else
			return builder.lessThan(attribute, value);
	}

	@Override
	public boolean matches(Issue issue, User user) {
		Integer fieldValue = (Integer) issue.getFieldValue(getFieldName());
		if (operator == IssueQueryLexer.Is)
			return Objects.equals(fieldValue, value);
		else if (operator == IssueQueryLexer.IsGreaterThan)
			return fieldValue != null && fieldValue > value;
		else
			return fieldValue != null && fieldValue < value;
	}

	@Override
	public boolean needsLogin() {
		return false;
	}

	@Override
	public String toString() {
		return IssueQuery.quote(getFieldName()) + " " + IssueQuery.getRuleName(operator) + " " + IssueQuery.quote(String.valueOf(value));
	}

	@Override
	public void fill(Issue issue, Set<String> initedLists) {
		issue.setFieldValue(getFieldName(), value);
	}

}
