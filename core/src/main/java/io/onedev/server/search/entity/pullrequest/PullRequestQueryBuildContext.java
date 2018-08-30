package io.onedev.server.search.entity.pullrequest;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Root;

import com.google.common.base.Splitter;

import io.onedev.server.model.PullRequest;
import io.onedev.server.search.entity.QueryBuildContext;

public class PullRequestQueryBuildContext extends QueryBuildContext<PullRequest> {
	
	public PullRequestQueryBuildContext(Root<PullRequest> root, CriteriaBuilder builder) {
		super(root, builder);
	}
	
	@Override
	public From<?, ?> newJoin(String joinName) {
		return joinByAttrs(Splitter.on(".").splitToList(joinName));
	}

}
