package io.onedev.server.search.entity.pullrequest;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import io.onedev.server.model.PullRequest;
import io.onedev.server.model.User;
import io.onedev.server.model.support.pullrequest.MergePreview;
import io.onedev.server.search.entity.EntityCriteria;
import io.onedev.server.util.query.PullRequestQueryConstants;

public class HasMergeConflictsCriteria extends EntityCriteria<PullRequest> {

	private static final long serialVersionUID = 1L;

	@Override
	public Predicate getPredicate(Root<PullRequest> root, CriteriaBuilder builder, User user) {
		Path<?> requestHead = PullRequestQuery.getPath(root, PullRequestQueryConstants.ATTR_LAST_MERGE_PREVIEW_REQUEST_HEAD);
		Path<?> merged = PullRequestQuery.getPath(root, PullRequestQueryConstants.ATTR_LAST_MERGE_PREVIEW_MERGED);
		return builder.and(
				builder.isNotNull(requestHead), 
				builder.isNull(merged));
	}

	@Override
	public boolean matches(PullRequest request, User user) {
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
