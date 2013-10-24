package com.pmease.gitop.core.gatekeeper.checkresult;

import java.util.Collection;
import java.util.List;

import com.pmease.gitop.core.gatekeeper.voteeligibility.VoteEligibility;

/* merge request acceptance check is pending and result is unknown yet */
@SuppressWarnings("serial")
public class Pending extends CheckResult {

	public Pending(List<String> reasons, Collection<VoteEligibility> voteEligibilities) {
		super(reasons, voteEligibilities);
	}

	public Pending(String reason, VoteEligibility voteEligibility) {
		super(reason, voteEligibility);
	}

	@Override
    public String toString() {
        return "Pending";
    }
    
}