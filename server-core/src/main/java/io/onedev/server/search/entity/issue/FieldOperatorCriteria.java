package io.onedev.server.search.entity.issue;

import java.util.Objects;
import java.util.Set;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;

import io.onedev.server.model.Issue;
import io.onedev.server.model.IssueField;
import io.onedev.server.model.User;
import io.onedev.server.util.SecurityUtils;

public class FieldOperatorCriteria extends FieldCriteria {

	private static final long serialVersionUID = 1L;
	
	private final int operator;

	public FieldOperatorCriteria(String name, int operator) {
		super(name);
		this.operator = operator;
	}

	@Override
	protected Predicate getValuePredicate(Join<?, ?> field, CriteriaBuilder builder, User user) {
		Path<?> attribute = field.get(IssueField.ATTR_VALUE);
		if (operator == IssueQueryLexer.IsEmpty)
			return builder.isNull(attribute);
		else
			return builder.equal(attribute, user.getName());
	}

	@Override
	public boolean matches(Issue issue, User user) {
		Object fieldValue = issue.getFieldValue(getFieldName());
		if (operator == IssueQueryLexer.IsEmpty)
			return fieldValue == null;
		else 
			return Objects.equals(fieldValue, user.getName());
	}

	@Override
	public boolean needsLogin() {
		return operator == IssueQueryLexer.IsMe;
	}

	@Override
	public String toString() {
		return IssueQuery.quote(getFieldName()) + " " + IssueQuery.getRuleName(operator);
	}

	@Override
	public void fill(Issue issue, Set<String> initedLists) {
		if (operator == IssueQueryLexer.IsEmpty)
			issue.setFieldValue(getFieldName(), null);
		else if (operator == IssueQueryLexer.IsMe)
			issue.setFieldValue(getFieldName(), SecurityUtils.getUser().getName());
	}

}
