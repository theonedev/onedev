package com.pmease.gitop.core.gatekeeper.voteeligibility;

import java.io.Serializable;

import com.pmease.gitop.core.model.MergeRequest;
import com.pmease.gitop.core.model.User;

public interface VoteEligibility extends Serializable {
    boolean canVote(User user, MergeRequest request);
}
