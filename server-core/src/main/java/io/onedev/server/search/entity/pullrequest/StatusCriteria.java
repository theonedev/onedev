package io.onedev.server.search.entity.pullrequest;

import io.onedev.server.model.PullRequest;
import io.onedev.server.model.PullRequest.Status;
import io.onedev.server.util.criteria.Criteria;

public abstract class StatusCriteria extends Criteria<PullRequest> {

	public abstract Status getStatus();
}
