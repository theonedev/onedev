package io.onedev.server.search.entity.issue;

import java.util.Objects;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Predicate;

import io.onedev.server.model.Issue;
import io.onedev.server.model.IssueField;


public class BooleanFieldCriteria extends FieldCriteria {

	private static final long serialVersionUID = 1L;

	private final boolean value;
	
	public BooleanFieldCriteria(String name, boolean value) {
		super(name);
		this.value = value;
	}

	@Override
	public Predicate getValuePredicate(Join<?, ?> field, CriteriaBuilder builder) {
		return builder.equal(field.get(IssueField.PROP_VALUE), String.valueOf(value));
	}

	@Override
	public boolean matches(Issue issue) {
		return Objects.equals(value, issue.getFieldValue(getFieldName()));
	}

	@Override
	public String toStringWithoutParens() {
		return quote(getFieldName()) + " " 
				+ IssueQuery.getRuleName(IssueQueryLexer.Is) + " " 
				+ quote(String.valueOf(value));
	}

	@Override
	public void fill(Issue issue) {
		issue.setFieldValue(getFieldName(), value);
	}

}
