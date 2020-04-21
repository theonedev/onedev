package io.onedev.server.search.entity.issue;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.annotation.Nullable;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Predicate;

import io.onedev.server.model.Build;
import io.onedev.server.model.Issue;
import io.onedev.server.model.IssueField;
import io.onedev.server.model.Project;
import io.onedev.server.search.entity.EntityQuery;

public class BuildFieldCriteria extends FieldCriteria {

	private static final long serialVersionUID = 1L;

	private final Build build;
	
	private final String value;
	
	private final boolean allowMultiple;
	
	public BuildFieldCriteria(String name, @Nullable Project project, String value, boolean allowMultiple) {
		super(name);
		build = EntityQuery.getBuild(project, value);
		this.value = value;
		this.allowMultiple = allowMultiple;
	}

	@Override
	protected Predicate getValuePredicate(Join<?, ?> field, CriteriaBuilder builder) {
		return builder.and(
				builder.equal(field.getParent().get(Issue.PROP_PROJECT), build.getProject()),
				builder.equal(field.get(IssueField.PROP_ORDINAL), build.getNumber()));
	}

	@Override
	public boolean matches(Issue issue) {
		Object fieldValue = issue.getFieldValue(getFieldName());
		return issue.getProject().equals(build.getProject()) && Objects.equals(fieldValue, build.getNumber());
	}

	@SuppressWarnings({"unchecked" })
	@Override
	public void fill(Issue issue) {
		if (allowMultiple) {
			List<Long> valueFromIssue = (List<Long>) issue.getFieldValue(getFieldName());
			if (valueFromIssue == null)
				valueFromIssue = new ArrayList<>();
			valueFromIssue.add(build.getNumber());
			issue.setFieldValue(getFieldName(), valueFromIssue);
		} else {
			issue.setFieldValue(getFieldName(), build.getNumber());
		}
	}
	
	@Override
	public String toStringWithoutParens() {
		return quote(getFieldName()) + " " 
				+ IssueQuery.getRuleName(IssueQueryLexer.Is) + " " 
				+ quote(value);
	}

}
