package io.onedev.server.search.entity.pullrequest;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import io.onedev.server.model.PullRequest;
import io.onedev.server.model.support.pullrequest.MergePreview;

import io.onedev.server.search.entity.EntityCriteria;

public class HasMergeConflictsCriteria extends EntityCriteria<PullRequest> {

	private static final long serialVersionUID = 1L;

	@Override
	public Predicate getPredicate(Root<PullRequest> root, CriteriaBuilder builder) {
		Path<?> requestHead = PullRequestQuery.getPath(root, PullRequest.PROP_LAST_MERGE_PREVIEW + "." + MergePreview.PROP_HEAD_COMMIT_HASH);
		Path<?> merged = PullRequestQuery.getPath(root, PullRequest.PROP_LAST_MERGE_PREVIEW + "." + MergePreview.PROP_MERGED_COMMIT_HASH);
		return builder.and(
				builder.isNotNull(requestHead), 
				builder.isNull(merged));
	}

	@Override
	public boolean matches(PullRequest request) {
		MergePreview preview = request.getLastMergePreview();
		return preview != null && preview.getMergeCommitHash() == null;
	}

	@Override
	public String toStringWithoutParens() {
		return PullRequestQuery.getRuleName(PullRequestQueryLexer.HasMergeConflicts);
	}

}
