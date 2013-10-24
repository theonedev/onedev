package com.pmease.gitop.core.gatekeeper.checkresult;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

import com.google.common.collect.Lists;
import com.pmease.gitop.core.gatekeeper.voteeligibility.NoneCanVote;
import com.pmease.gitop.core.gatekeeper.voteeligibility.VoteEligibility;
import com.pmease.gitop.core.model.MergeRequest;
import com.pmease.gitop.core.model.User;

@SuppressWarnings("serial")
public abstract class CheckResult implements Serializable {
	
	private final List<String> reasons;

	private final Collection<VoteEligibility> voteEligibilities;
	
    public CheckResult(List<String> reasons, Collection<VoteEligibility> voteEligibilities) {
        this.reasons = reasons;
        this.voteEligibilities = voteEligibilities;
    }

    public CheckResult(List<String> reasons) {
        this.reasons = reasons;
        voteEligibilities = Lists.newArrayList((VoteEligibility) new NoneCanVote());
    }

    public CheckResult(String reason, VoteEligibility voteEligibility) {
        this.reasons = Lists.newArrayList(reason);
        
        this.voteEligibilities = Lists.newArrayList(voteEligibility);
    }

    public CheckResult(String reason) {
        this.reasons = Lists.newArrayList(reason);
        voteEligibilities = Lists.newArrayList((VoteEligibility) new NoneCanVote());
    }

    public List<String> getReasons() {
		return reasons;
	}
	
    public Collection<VoteEligibility> getVoteEligibilities() {
        return voteEligibilities;
    }

    public boolean canVote(User user, MergeRequest request) {
        if (user.equals(request.getSubmitter()))
            return false;
        
        for (VoteEligibility each: voteEligibilities) {
            if (each.canVote(user, request))
                return true;
        }
        return false;
    }
    
}