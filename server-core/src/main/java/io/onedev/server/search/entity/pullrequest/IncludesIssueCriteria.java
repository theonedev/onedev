package io.onedev.server.search.entity.pullrequest;

import java.util.Collection;
import java.util.HashSet;

import javax.annotation.Nullable;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.eclipse.jgit.lib.ObjectId;

import io.onedev.server.OneDev;
import io.onedev.server.infomanager.PullRequestInfoManager;
import io.onedev.server.infomanager.CommitInfoManager;
import io.onedev.server.model.Issue;
import io.onedev.server.model.Project;
import io.onedev.server.model.PullRequest;
import io.onedev.server.search.entity.EntityCriteria;
import io.onedev.server.search.entity.EntityQuery;

public class IncludesIssueCriteria extends EntityCriteria<PullRequest> {

	private static final long serialVersionUID = 1L;

	private final Issue issue;
	
	private final String value;
	
	public IncludesIssueCriteria(@Nullable Project project, String value) {
		issue = EntityQuery.getIssue(project, value);
		this.value = value;
	}
	
	@Override
	public Predicate getPredicate(Root<PullRequest> root, CriteriaBuilder builder) {
		Collection<Long> pullRequestIds = getPullRequestIds(issue.getProject());
		if (!pullRequestIds.isEmpty()) {
			return builder.and(
					builder.equal(root.get(PullRequest.PROP_TARGET_PROJECT), issue.getProject()),
					root.get(PullRequest.PROP_ID).in(pullRequestIds));
		} else {
			return builder.disjunction();
		}
	}
	
	private Collection<Long> getPullRequestIds(Project project) {
		Collection<Long> pullRequestIds = new HashSet<>();
		for (ObjectId commit: OneDev.getInstance(CommitInfoManager.class).getFixCommits(project, issue.getNumber()))
			pullRequestIds.addAll(OneDev.getInstance(PullRequestInfoManager.class).getPullRequestIds(project, commit));
		return pullRequestIds;
	}
	
	@Override
	public boolean matches(PullRequest request) {
		return request.getTargetProject().equals(issue.getProject()) 
				&& getPullRequestIds(request.getTargetProject()).contains(request.getId());
	}

	@Override
	public String toStringWithoutParens() {
		return PullRequestQuery.getRuleName(PullRequestQueryLexer.IncludesIssue) + " " + quote(value);
	}

}
