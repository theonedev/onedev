package io.onedev.server.search.entity.pullrequest;

import javax.annotation.Nullable;

import io.onedev.server.model.PullRequest;
import io.onedev.server.model.User;
import io.onedev.server.util.criteria.Criteria;

public abstract class SubmittedByCriteria extends Criteria<PullRequest> {

    @Nullable
    public abstract User getUser();
    
}
