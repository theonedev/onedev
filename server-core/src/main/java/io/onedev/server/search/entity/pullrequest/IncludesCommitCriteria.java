package io.onedev.server.search.entity.pullrequest;

import java.util.Collection;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.eclipse.jgit.lib.ObjectId;

import io.onedev.server.OneDev;
import io.onedev.server.cache.CodeCommentRelationInfoManager;
import io.onedev.server.model.Project;
import io.onedev.server.model.PullRequest;
import io.onedev.server.model.User;
import io.onedev.server.util.PullRequestConstants;

public class IncludesCommitCriteria extends PullRequestCriteria {

	private static final long serialVersionUID = 1L;

	private final ObjectId commitId;
	
	public IncludesCommitCriteria(ObjectId commitId) {
		this.commitId = commitId;
	}
	
	@Override
	public Predicate getPredicate(Project project, Root<PullRequest> root, CriteriaBuilder builder, User user) {
		Collection<Long> pullRequestIds = getPullRequestIds(project);
		if (!pullRequestIds.isEmpty())
			return root.get(PullRequestConstants.ATTR_ID).in(pullRequestIds);
		else
			return builder.disjunction();
	}
	
	private Collection<Long> getPullRequestIds(Project project) {
		return OneDev.getInstance(CodeCommentRelationInfoManager.class).getPullRequestIds(project, commitId);		
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
		return PullRequestQuery.getRuleName(PullRequestQueryLexer.IncludesCommit) + " " 
				+ PullRequestQuery.quote(commitId.name());
	}

}
