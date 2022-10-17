package io.onedev.server.search.entity.pullrequest;

import java.util.Collection;
import java.util.HashSet;

import javax.annotation.Nullable;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Predicate;

import org.eclipse.jgit.lib.ObjectId;

import io.onedev.server.OneDev;
import io.onedev.server.infomanager.CommitInfoManager;
import io.onedev.server.infomanager.PullRequestInfoManager;
import io.onedev.server.model.Issue;
import io.onedev.server.model.Project;
import io.onedev.server.model.PullRequest;
import io.onedev.server.search.entity.EntityQuery;
import io.onedev.server.util.criteria.Criteria;

public class IncludesIssueCriteria extends Criteria<PullRequest> {

	private static final long serialVersionUID = 1L;

	private final Issue issue;
	
	private final String value;
	
	public IncludesIssueCriteria(@Nullable Project project, String value) {
		issue = EntityQuery.getIssue(project, value);
		this.value = value;
	}
	
	public IncludesIssueCriteria(Issue issue) {
		this.issue = issue;
		value = String.valueOf(issue.getNumber());
	}
	
	@Override
	public Predicate getPredicate(CriteriaQuery<?> query, From<PullRequest, PullRequest> from, 
			CriteriaBuilder builder) {
		Collection<Long> pullRequestIds = new HashSet<>();
		issue.getProject().getTree().stream().filter(it->it.isCodeManagement()).forEach(it-> {
			pullRequestIds.addAll(getPullRequestIds(it));
		});
		if (!pullRequestIds.isEmpty()) 
			return from.get(PullRequest.PROP_ID).in(pullRequestIds);
		else 
			return builder.disjunction();
	}
	
	private Collection<Long> getPullRequestIds(Project project) {
		Collection<Long> pullRequestIds = new HashSet<>();
		for (ObjectId commit: OneDev.getInstance(CommitInfoManager.class).getFixCommits(project.getId(), issue.getId()))
			pullRequestIds.addAll(OneDev.getInstance(PullRequestInfoManager.class).getPullRequestIds(project, commit));
		return pullRequestIds;
	}
	
	@Override
	public boolean matches(PullRequest request) {
		return getPullRequestIds(request.getProject()).contains(request.getId());
	}

	@Override
	public String toStringWithoutParens() {
		return PullRequestQuery.getRuleName(PullRequestQueryLexer.IncludesIssue) + " " + quote(value);
	}

}
