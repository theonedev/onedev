package io.onedev.server.search.entity.pullrequest;

import org.jspecify.annotations.Nullable;

import io.onedev.server.model.PullRequest;
import io.onedev.server.model.User;
import io.onedev.server.util.criteria.Criteria;

public abstract class ApprovedByCriteria extends Criteria<PullRequest> {

    @Nullable
    public abstract User getUser();
    
}
