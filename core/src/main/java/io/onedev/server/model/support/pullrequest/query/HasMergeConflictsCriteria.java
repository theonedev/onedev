package io.onedev.server.model.support.pullrequest.query;

import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;

import io.onedev.server.model.Project;
import io.onedev.server.model.PullRequest;
import io.onedev.server.model.support.pullrequest.MergePreview;
import io.onedev.server.util.query.QueryBuildContext;

public class HasMergeConflictsCriteria extends PullRequestCriteria {

	private static final long serialVersionUID = 1L;

	@Override
	public Predicate getPredicate(Project project, QueryBuildContext<PullRequest> context) {
		Path<?> requestHead = PullRequestQuery.getPath(context.getRoot(), PullRequest.PATH_LAST_MERGE_PREVIEW_REQUEST_HEAD);
		Path<?> merged = PullRequestQuery.getPath(context.getRoot(), PullRequest.PATH_LAST_MERGE_PREVIEW_MERGED);
		return context.getBuilder().and(
				context.getBuilder().isNotNull(requestHead), 
				context.getBuilder().isNull(merged));
	}

	@Override
	public boolean matches(PullRequest request) {
		MergePreview preview = request.getLastMergePreview();
		return preview != null && preview.getMerged() == null;
	}

	@Override
	public boolean needsLogin() {
		return false;
	}

	@Override
	public String toString() {
		return PullRequestQuery.getRuleName(PullRequestQueryLexer.HasMergeConflicts);
	}

}
