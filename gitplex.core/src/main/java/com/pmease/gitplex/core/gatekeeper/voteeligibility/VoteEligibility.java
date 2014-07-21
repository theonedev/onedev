package com.pmease.gitplex.core.gatekeeper.voteeligibility;

import java.io.Serializable;

import com.pmease.gitplex.core.model.PullRequest;
import com.pmease.gitplex.core.model.User;

public interface VoteEligibility extends Serializable {
    boolean canVote(User user, PullRequest request);
}
