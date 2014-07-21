package com.pmease.gitplex.core.gatekeeper.checkresult;

import java.util.Collection;
import java.util.List;

import com.pmease.gitplex.core.gatekeeper.voteeligibility.VoteEligibility;

/* 
 * Same as Pending, but followed gate keeper should not be checked unless result 
 * of this gate keeper has been determined.
 */
@SuppressWarnings("serial")
public class PendingAndBlock extends CheckResult {
	
	public PendingAndBlock(List<String> reasons, Collection<VoteEligibility> voteEligibilities) {
		super(reasons, voteEligibilities);
	}

	public PendingAndBlock(String reason, VoteEligibility voteEligibility) {
		super(reason, voteEligibility);
	}

	@Override
    public String toString() {
        return "Blocked";
    }
    
}