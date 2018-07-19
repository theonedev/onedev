package io.onedev.server.model.support.pullrequest.query;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.criteria.Predicate;

import org.eclipse.jgit.lib.ObjectId;

import io.onedev.server.OneDev;
import io.onedev.server.manager.CodeCommentRelationInfoManager;
import io.onedev.server.manager.PullRequestManager;
import io.onedev.server.model.Project;
import io.onedev.server.model.PullRequest;
import io.onedev.server.util.query.QueryBuildContext;

public class ContainsCommitCriteria extends PullRequestCriteria {

	private static final long serialVersionUID = 1L;

	private final ObjectId commitId;
	
	public ContainsCommitCriteria(ObjectId commitId) {
		this.commitId = commitId;
	}
	
	@Override
	public Predicate getPredicate(Project project, QueryBuildContext<PullRequest> context) {
		Collection<Long> ids = new HashSet<>();
		for (String uuid: getUUIDs(project, commitId)) {
			PullRequest request = OneDev.getInstance(PullRequestManager.class).find(uuid);
			if (request != null)
				ids.add(request.getId());
		}
		if (!ids.isEmpty())
			return context.getRoot().get(PullRequest.PATH_ID).in(ids);
		else
			return context.getBuilder().disjunction();
	}
	
	private Set<String> getUUIDs(Project project, ObjectId commitId) {
		return OneDev.getInstance(CodeCommentRelationInfoManager.class).getPullRequestUUIDs(project, commitId);		
	}
	
	@Override
	public boolean matches(PullRequest request) {
		return getUUIDs(request.getTargetProject(), commitId).contains(request.getUUID());
	}

	@Override
	public boolean needsLogin() {
		return false;
	}

	@Override
	public String toString() {
		return PullRequestQuery.getRuleName(PullRequestQueryLexer.ContainsCommit) + " " + PullRequestQuery.quote(commitId.name());
	}

}
