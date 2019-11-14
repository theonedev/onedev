package io.onedev.server.search.entity.issue;

import java.util.Objects;
import java.util.Set;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Predicate;

import io.onedev.server.model.Issue;
import io.onedev.server.model.IssueField;
import io.onedev.server.model.User;

public class BooleanFieldCriteria extends FieldCriteria {

	private static final long serialVersionUID = 1L;

	private final boolean value;
	
	public BooleanFieldCriteria(String name, boolean value) {
		super(name);
		this.value = value;
	}

	@Override
	public Predicate getValuePredicate(Join<?, ?> field, CriteriaBuilder builder, User user) {
		return builder.equal(field.get(IssueField.ATTR_VALUE), String.valueOf(value));
	}

	@Override
	public boolean matches(Issue issue, User user) {
		return Objects.equals(value, issue.getFieldValue(getFieldName()));
	}

	@Override
	public boolean needsLogin() {
		return false;
	}

	@Override
	public String toString() {
		return IssueQuery.quote(getFieldName()) + " " + IssueQuery.getRuleName(IssueQueryLexer.Is) + " " + IssueQuery.quote(String.valueOf(value));
	}

	@Override
	public void fill(Issue issue, Set<String> initedLists) {
		issue.setFieldValue(getFieldName(), value);
	}

}
