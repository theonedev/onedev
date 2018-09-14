package io.onedev.server.search.entity.pullrequest;

import java.util.Collection;

import javax.persistence.criteria.Predicate;

import org.eclipse.jgit.lib.ObjectId;

import io.onedev.server.OneDev;
import io.onedev.server.manager.CodeCommentRelationInfoManager;
import io.onedev.server.model.Project;
import io.onedev.server.model.PullRequest;
import io.onedev.server.model.User;
import io.onedev.server.model.support.pullrequest.PullRequestConstants;
import io.onedev.server.search.entity.QueryBuildContext;

public class CommitCriteria extends PullRequestCriteria {

	private static final long serialVersionUID = 1L;

	private final ObjectId commitId;
	
	public CommitCriteria(ObjectId commitId) {
		this.commitId = commitId;
	}
	
	@Override
	public Predicate getPredicate(Project project, QueryBuildContext<PullRequest> context, User user) {
		Collection<Long> pullRequestIds = getPullRequestIds(project, commitId);
		if (!pullRequestIds.isEmpty())
			return context.getRoot().get(PullRequestConstants.ATTR_ID).in(pullRequestIds);
		else
			return context.getBuilder().disjunction();
	}
	
	private Collection<Long> getPullRequestIds(Project project, ObjectId commitId) {
		return OneDev.getInstance(CodeCommentRelationInfoManager.class).getPullRequestIds(project, commitId);		
	}
	
	@Override
	public boolean matches(PullRequest request, User user) {
		return getPullRequestIds(request.getTargetProject(), commitId).contains(request.getId());
	}

	@Override
	public boolean needsLogin() {
		return false;
	}

	@Override
	public String toString() {
		return PullRequestQuery.quote(PullRequestConstants.FIELD_COMMIT) + " " 
				+ PullRequestQuery.getRuleName(PullRequestQueryLexer.Contains) + " " 
				+ PullRequestQuery.quote(commitId.name());
	}

}
