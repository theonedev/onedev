package io.onedev.server.search.entity.issue;

import java.util.Collection;

import org.jspecify.annotations.Nullable;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Predicate;

import io.onedev.server.model.Issue;
import io.onedev.server.model.Project;
import io.onedev.server.model.PullRequest;
import io.onedev.server.search.entity.EntityQuery;
import io.onedev.server.util.ProjectScope;
import io.onedev.server.util.criteria.Criteria;

public class FixedInPullRequestCriteria extends Criteria<Issue> {

	private static final long serialVersionUID = 1L;

	private final Project project;
	
	private final String value;
	
	private transient PullRequest request;
	
	public FixedInPullRequestCriteria(@Nullable Project project, String value) {
		this.project = project;
		this.value = value;
	}

	public FixedInPullRequestCriteria(PullRequest request) {
		this.request = request;
		project = request.getProject();
		value = request.getReference().toString(null);
	}
	
	public PullRequest getRequest() {
		if (request == null)
			request = EntityQuery.getPullRequest(project, value);
		return request;
	}
	
	@Override
	public Predicate getPredicate(@Nullable ProjectScope projectScope, CriteriaQuery<?> query, From<Issue, Issue> from, CriteriaBuilder builder) {
		Collection<Long> fixedIssueIds = getRequest().getFixedIssueIds();
		if (!fixedIssueIds.isEmpty()) 
			return from.get(Issue.PROP_ID).in(fixedIssueIds);
		else 
			return builder.disjunction();
	}

	@Override
	public boolean matches(Issue issue) {
		return getRequest().getFixedIssueIds().contains(issue.getId());
	}

	@Override
	public String toStringWithoutParens() {
		return IssueQuery.getRuleName(IssueQueryLexer.FixedInPullRequest) + " " + quote(value);
	}

}
