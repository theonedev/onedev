package io.onedev.server.search.entity.issue;

import java.util.Objects;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Predicate;

import io.onedev.server.model.Issue;
import io.onedev.server.model.IssueField;
import io.onedev.server.model.Project;
import io.onedev.server.model.User;

public class ReferenceableFieldCriteria extends FieldCriteria {

	private static final long serialVersionUID = 1L;

	private final long value;
	
	public ReferenceableFieldCriteria(String name, long value) {
		super(name);
		this.value = value;
	}

	@Override
	protected Predicate getValuePredicate(Project project, Join<?, ?> field, CriteriaBuilder builder, User user) {
		return builder.equal(field.get(IssueField.ATTR_ORDINAL), value);
	}

	@Override
	public boolean matches(Issue issue, User user) {
		Object fieldValue = issue.getFieldValue(getFieldName());
		return Objects.equals(fieldValue, value);
	}

	@Override
	public boolean needsLogin() {
		return false;
	}

	@Override
	public String toString() {
		return IssueQuery.quote(getFieldName()) + " " + IssueQuery.getRuleName(IssueQueryLexer.Is) + " " + IssueQuery.quote(String.valueOf(value));
	}

}
