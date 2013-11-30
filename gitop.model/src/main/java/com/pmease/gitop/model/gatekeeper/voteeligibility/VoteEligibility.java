package com.pmease.gitop.model.gatekeeper.voteeligibility;

import java.io.Serializable;

import com.pmease.gitop.model.PullRequest;
import com.pmease.gitop.model.User;

public interface VoteEligibility extends Serializable {
    boolean canVote(User user, PullRequest request);
}
