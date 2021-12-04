package io.onedev.server.search.entity.issue;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.annotation.Nullable;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Predicate;

import io.onedev.server.model.Build;
import io.onedev.server.model.Issue;
import io.onedev.server.model.IssueField;
import io.onedev.server.model.Project;
import io.onedev.server.search.entity.EntityQuery;

public class BuildFieldCriteria extends FieldCriteria {

	private static final long serialVersionUID = 1L;

	private final Project project;
	
	private final String value;
	
	private final boolean allowMultiple;
	
	private transient Build build;
	
	public BuildFieldCriteria(String name, @Nullable Project project, String value, boolean allowMultiple) {
		super(name);
		this.project = project;
		this.value = value;
		this.allowMultiple = allowMultiple;
	}
	
	private Build getBuild() {
		if (build == null)
			build = EntityQuery.getBuild(project, value);
		return build;
	}

	@Override
	protected Predicate getValuePredicate(From<Issue, Issue> issueFrom, From<IssueField, IssueField> fieldFrom, CriteriaBuilder builder) {
		return builder.equal(fieldFrom.get(IssueField.PROP_ORDINAL), getBuild().getId());
	}

	@Override
	public boolean matches(Issue issue) {
		Object fieldValue = issue.getFieldValue(getFieldName());
		return issue.getProject().equals(getBuild().getProject()) && Objects.equals(fieldValue, getBuild().getId());
	}

	@SuppressWarnings({"unchecked" })
	@Override
	public void fill(Issue issue) {
		if (allowMultiple) {
			List<Long> valueFromIssue = (List<Long>) issue.getFieldValue(getFieldName());
			if (valueFromIssue == null)
				valueFromIssue = new ArrayList<>();
			valueFromIssue.add(getBuild().getId());
			issue.setFieldValue(getFieldName(), valueFromIssue);
		} else {
			issue.setFieldValue(getFieldName(), getBuild().getId());
		}
	}
	
	@Override
	public String toStringWithoutParens() {
		return quote(getFieldName()) + " " 
				+ IssueQuery.getRuleName(IssueQueryLexer.Is) + " " 
				+ quote(value);
	}

}
