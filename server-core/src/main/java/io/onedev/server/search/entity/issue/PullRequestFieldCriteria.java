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
import io.onedev.server.model.User;
import io.onedev.server.search.entity.EntityQuery;
import io.onedev.server.util.query.IssueQueryConstants;

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
	protected Predicate getValuePredicate(Join<?, ?> field, CriteriaBuilder builder, User user) {
		return builder.and(
				builder.equal(field.getParent().get(IssueQueryConstants.ATTR_PROJECT), request.getTargetProject()),
				builder.equal(field.get(IssueField.ATTR_ORDINAL), request.getNumber()));
	}

	@Override
	public boolean matches(Issue issue, User user) {
		Object fieldValue = issue.getFieldValue(getFieldName());
		return issue.getProject().equals(request.getTargetProject()) && Objects.equals(fieldValue, request.getNumber());
	}

	@Override
	public boolean needsLogin() {
		return false;
	}

	@Override
	public String toString() {
		return IssueQuery.quote(getFieldName()) + " " 
				+ IssueQuery.getRuleName(IssueQueryLexer.Is) + " " 
				+ IssueQuery.quote(value);
	}

}
