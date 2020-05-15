package io.onedev.server.search.entity.pullrequest;

import java.util.Collection;

import javax.annotation.Nullable;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.eclipse.jgit.lib.ObjectId;

import io.onedev.server.OneDev;
import io.onedev.server.infomanager.PullRequestInfoManager;
import io.onedev.server.model.Project;
import io.onedev.server.model.PullRequest;
import io.onedev.server.search.entity.EntityCriteria;
import io.onedev.server.search.entity.EntityQuery;
import io.onedev.server.util.ProjectScopedCommit;

public class IncludesCommitCriteria extends EntityCriteria<PullRequest> {

	private static final long serialVersionUID = 1L;

	private final Project project;
	
	private final ObjectId commitId;
	
	private final String value;
	
	public IncludesCommitCriteria(@Nullable Project project, String value) {
		ProjectScopedCommit commitId = EntityQuery.getCommitId(project, value);
		this.project = commitId.getProject();
		this.commitId = commitId.getCommitId();
		this.value = value;
	}
	
	@Override
	public Predicate getPredicate(Root<PullRequest> root, CriteriaBuilder builder) {
		Collection<Long> pullRequestIds = getPullRequestIds();
		if (!pullRequestIds.isEmpty()) {
			return builder.and(
					builder.equal(root.get(PullRequest.PROP_TARGET_PROJECT), project),
					root.get(PullRequest.PROP_ID).in(pullRequestIds));
		} else {
			return builder.disjunction();
		}
	}
	
	private Collection<Long> getPullRequestIds() {
		return OneDev.getInstance(PullRequestInfoManager.class).getPullRequestIds(project, commitId);		
	}
	
	@Override
	public boolean matches(PullRequest request) {
		return request.getTargetProject().equals(project) && getPullRequestIds().contains(request.getId());
	}

	@Override
	public String toStringWithoutParens() {
		return PullRequestQuery.getRuleName(PullRequestQueryLexer.IncludesCommit) + " " + quote(value);
	}

}
