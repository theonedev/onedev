package io.onedev.server.search.entity.pullrequest;

import java.util.Collection;
import java.util.HashSet;

import javax.persistence.criteria.Predicate;

import org.eclipse.jgit.lib.ObjectId;

import io.onedev.server.OneDev;
import io.onedev.server.manager.CodeCommentRelationInfoManager;
import io.onedev.server.manager.CommitInfoManager;
import io.onedev.server.model.Issue;
import io.onedev.server.model.Project;
import io.onedev.server.model.PullRequest;
import io.onedev.server.model.User;
import io.onedev.server.search.entity.QueryBuildContext;
import io.onedev.server.util.PullRequestConstants;

public class IncludesIssueCriteria extends PullRequestCriteria {

	private static final long serialVersionUID = 1L;

	private Issue value;
	
	public IncludesIssueCriteria(Issue value) {
		this.value = value;
	}
	
	@Override
	public Predicate getPredicate(Project project, QueryBuildContext<PullRequest> context, User user) {
		Collection<Long> pullRequestIds = getPullRequestIds(project);
		if (!pullRequestIds.isEmpty())
			return context.getRoot().get(PullRequestConstants.ATTR_ID).in(pullRequestIds);
		else
			return context.getBuilder().disjunction();
	}
	
	private Collection<Long> getPullRequestIds(Project project) {
		Collection<Long> pullRequestIds = new HashSet<>();
		for (ObjectId commit: OneDev.getInstance(CommitInfoManager.class).getFixCommits(project, value.getNumber()))
			pullRequestIds.addAll(OneDev.getInstance(CodeCommentRelationInfoManager.class).getPullRequestIds(project, commit));
		return pullRequestIds;
	}
	
	@Override
	public boolean matches(PullRequest request, User user) {
		return getPullRequestIds(request.getTargetProject()).contains(request.getId());
	}

	@Override
	public boolean needsLogin() {
		return false;
	}

	@Override
	public String toString() {
		return PullRequestQuery.getRuleName(PullRequestQueryLexer.IncludesIssue) + " " 
				+ PullRequestQuery.quote("#" + value.getNumber());
	}

}
