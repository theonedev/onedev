package com.pmease.gitop.model.gatekeeper.checkresult;

import java.util.Collection;
import java.util.List;

import com.pmease.gitop.model.gatekeeper.voteeligibility.VoteEligibility;

/* 
 * Same as Pending, but followed gate keeper should not be checked unless result 
 * of this gate keeper has been determined.
 */
@SuppressWarnings("serial")
public class BlockedPending extends CheckResult {
	
	public BlockedPending(List<String> reasons, Collection<VoteEligibility> voteEligibilities) {
		super(reasons, voteEligibilities);
	}

	public BlockedPending(String reason, VoteEligibility voteEligibility) {
		super(reason, voteEligibility);
	}

	@Override
    public String toString() {
        return "Blocked";
    }
    
}