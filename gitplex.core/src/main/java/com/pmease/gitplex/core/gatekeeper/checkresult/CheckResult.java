package com.pmease.gitplex.core.gatekeeper.checkresult;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

import com.google.common.collect.Lists;
import com.pmease.gitplex.core.gatekeeper.voteeligibility.NoneCanVote;
import com.pmease.gitplex.core.gatekeeper.voteeligibility.VoteEligibility;
import com.pmease.gitplex.core.model.PullRequest;
import com.pmease.gitplex.core.model.User;

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

    public boolean canVote(User user, PullRequest request) {
        if (user.equals(request.getSubmitter()))
            return false;
        
        for (VoteEligibility each: voteEligibilities) {
            if (each.canVote(user, request))
                return true;
        }
        return false;
    }
    
    /**
     * Whether or not the check represents for a disapproval.
     * 
     * @return
     * 			<tt>true</tt> if disapproved
     */
    public boolean isDisapproved() {
    	return this instanceof Disapproved;
    }
    
    /**
     * Whether or not the check represents for an approval.
     * 
     * @return
     * 			<tt>true</tt> if approved
     */
    public boolean isApproved() {
    	return this instanceof Approved;
    }
    
    /**
     * Whether or not the check represents for a pending approval.
     * 
     * @return
     * 			<tt>true</tt> if pending approval
     */
    public boolean isPending() {
    	return this instanceof Pending;
    }

    /**
     * Whether or not the check represents for a pending approval with block.
     * 
     * @return
     * 			<tt>true</tt> if pending approval with block
     */
    public boolean isPendingAndBlock() {
    	return this instanceof PendingAndBlock;
    }
    
    /**
     * Whether or not this check result should be ignored.
     * 
     * @return
     * 			<tt>true</tt> if this check result should be ignored
     */
    public boolean isIgnored() {
    	return this instanceof Ignored;
    }
    
    public boolean allowIntegration() {
    	return isApproved() || isIgnored();
    }
    
}