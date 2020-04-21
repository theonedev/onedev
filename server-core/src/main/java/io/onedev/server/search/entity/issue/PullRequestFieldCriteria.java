package io.onedev.server.search.entity.issue;

import java.util.Objects;

import javax.annotation.Nullable;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Predicate;

import io.onedev.server.model.Issue;
import io.onedev.server.model.IssueField;
import io.onedev.server.model.Project;
import io.onedev.server.model.PullRequest;
import io.onedev.server.search.entity.EntityQuery;

public class PullRequestFieldCriteria extends FieldCriteria {

	private static final long serialVersionUID = 1L;

	private final PullRequest request;
	
	private final String value;
	
	public PullRequestFieldCriteria(String name, @Nullable Project project, String value) {
		super(name);
		request = EntityQuery.getPullRequest(project, value);
		this.value = value;
	}

	@Override
	protected Predicate getValuePredicate(Join<?, ?> field, CriteriaBuilder builder) {
		return builder.and(
				builder.equal(field.getParent().get(Issue.PROP_PROJECT), request.getTargetProject()),
				builder.equal(field.get(IssueField.PROP_ORDINAL), request.getNumber()));
	}

	@Override
	public boolean matches(Issue issue) {
		Object fieldValue = issue.getFieldValue(getFieldName());
		return issue.getProject().equals(request.getTargetProject()) && Objects.equals(fieldValue, request.getNumber());
	}

	@Override
	public void fill(Issue issue) {
		issue.setFieldValue(getFieldName(), request.getNumber());
	}
	
	@Override
	public String toStringWithoutParens() {
		return quote(getFieldName()) + " " 
				+ IssueQuery.getRuleName(IssueQueryLexer.Is) + " " 
				+ quote(value);
	}

}
