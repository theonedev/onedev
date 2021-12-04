package io.onedev.server.search.entity.issue;

import java.util.Collection;

import javax.annotation.Nullable;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Predicate;

import io.onedev.server.model.Issue;
import io.onedev.server.model.Project;
import io.onedev.server.model.PullRequest;
import io.onedev.server.search.entity.EntityQuery;
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
		value = request.getFQN().toString();
	}
	
	public PullRequest getRequest() {
		if (request == null)
			request = EntityQuery.getPullRequest(project, value);
		return request;
	}
	
	@Override
	public Predicate getPredicate(CriteriaQuery<?> query, From<Issue, Issue> from, CriteriaBuilder builder) {
		Collection<Long> fixedIssueNumbers = getRequest().getFixedIssueNumbers();
		if (!fixedIssueNumbers.isEmpty()) {
			return builder.and(
					builder.equal(from.get(Issue.PROP_PROJECT), getRequest().getTargetProject()),
					from.get(Issue.PROP_NUMBER).in(fixedIssueNumbers));
		} else {
			return builder.disjunction();
		}
	}

	@Override
	public boolean matches(Issue issue) {
		return issue.getProject().equals(getRequest().getTargetProject()) 
				&& getRequest().getFixedIssueNumbers().contains(issue.getNumber());
	}

	@Override
	public String toStringWithoutParens() {
		return IssueQuery.getRuleName(IssueQueryLexer.FixedInPullRequest) + " " + quote(value);
	}

}
