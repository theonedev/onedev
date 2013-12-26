package com.pmease.gitop.model.gatekeeper;

import java.util.Collection;
import java.util.List;

import javax.annotation.Nullable;

import com.google.common.base.Preconditions;
import com.pmease.commons.editable.annotation.Editable;
import com.pmease.gitop.model.Project;
import com.pmease.gitop.model.PullRequest;
import com.pmease.gitop.model.gatekeeper.checkresult.Accepted;
import com.pmease.gitop.model.gatekeeper.checkresult.Blocked;
import com.pmease.gitop.model.gatekeeper.checkresult.CheckResult;
import com.pmease.gitop.model.gatekeeper.checkresult.Pending;
import com.pmease.gitop.model.gatekeeper.checkresult.Rejected;
import com.pmease.gitop.model.gatekeeper.voteeligibility.VoteEligibility;

@SuppressWarnings("serial")
@Editable
public abstract class AbstractGateKeeper implements GateKeeper {

	private boolean enabled = true;
	
	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}
	
	@Override
	public CheckResult check(PullRequest request) {
		if (enabled)
			return doCheck(request);
		else
			return accepted("Gate keeper is disabled.");
	}
	
	/**
	 * Check the gate keeper without considering enable flag. 
	 * 
	 * @param request
	 *			pull request to be checked 		
	 * @return
	 * 			result of the check
	 */
	protected abstract CheckResult doCheck(PullRequest request);

	@Override
	public final Object trim(@Nullable Object context) {
		Preconditions.checkArgument(context instanceof Project);
		return trim((Project)context);
	}
	
	protected GateKeeper trim(Project project) {
		return this;
	}

	protected CheckResult accepted(String reason) {
		return new Accepted(reason);
	}

	protected CheckResult rejected(String reason) {
		return new Rejected(reason);
	}

	protected CheckResult pending(String reason, VoteEligibility voteEligibility) {
		return new Pending(reason, voteEligibility);
	}

	protected CheckResult blocked(String reason, VoteEligibility voteEligibility) {
		return new Blocked(reason, voteEligibility);
	}

	protected CheckResult accepted(List<String> reasons) {
		return new Accepted(reasons);
	}

	protected CheckResult rejected(List<String> reasons) {
		return new Rejected(reasons);
	}

	protected CheckResult pending(List<String> reasons, Collection<VoteEligibility> voteEligibilies) {
		return new Pending(reasons, voteEligibilies);
	}

	protected CheckResult blocked(List<String> reasons, Collection<VoteEligibility> voteEligibilities) {
		return new Blocked(reasons, voteEligibilities);
	}

}
