package io.onedev.server.entityquery.pullrequest;

import java.util.List;

import javax.annotation.Nullable;

import io.onedev.server.entityquery.EntityCriteria;
import io.onedev.server.model.PullRequest;

public abstract class PullRequestCriteria extends EntityCriteria<PullRequest> {
	
	private static final long serialVersionUID = 1L;
	
	@Nullable
	public static PullRequestCriteria of(List<PullRequestCriteria> criterias) {
		if (criterias.size() > 1)
			return new AndCriteria(criterias);
		else if (criterias.size() == 1)
			return criterias.iterator().next();
		else
			return null;
	}
	
}
