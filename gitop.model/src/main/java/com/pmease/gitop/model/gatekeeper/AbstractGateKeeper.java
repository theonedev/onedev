package com.pmease.gitop.model.gatekeeper;

import java.util.Collection;
import java.util.List;

import javax.annotation.Nullable;

import com.google.common.base.Preconditions;
import com.pmease.commons.editable.annotation.Editable;
import com.pmease.gitop.model.Branch;
import com.pmease.gitop.model.Project;
import com.pmease.gitop.model.PullRequest;
import com.pmease.gitop.model.User;
import com.pmease.gitop.model.gatekeeper.checkresult.Accepted;
import com.pmease.gitop.model.gatekeeper.checkresult.PendingAndBlock;
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
	public CheckResult checkRequest(PullRequest request) {
		if (enabled)
			return doCheckRequest(request);
		else
			return accepted("Gate keeper is disabled.");
	}
	
	@Override
	public CheckResult checkFile(User user, Branch branch, @Nullable String file) {
		if (isEnabled())
			return doCheckFile(user, branch, file);
		else
			return accepted("Gate keeper is disabled");
	}
	
	@Override
	public CheckResult checkCommit(User user, Branch branch, String commit) {
		if (isEnabled())
			return doCheckCommit(user, branch, commit);
		else
			return accepted("Gate keeper is disabled");
	}

	/**
	 * Check the gate keeper against specified request without considering enable flag. This is 
	 * typically used to determine whether or not to accept a pull request. 
	 * 
	 * @param request
	 *			pull request to be checked
	 * @return
	 * 			result of the check
	 */
	protected abstract CheckResult doCheckRequest(PullRequest request);
	
	/**
	 * Check the gate keeper against specified user, branch and file without considering enable flag.
	 * This is typically used to determine whether or not to accept a file modification or branch 
	 * deletion (when file parameter is specified as <tt>null</tt>).
	 *
	 * @param user
	 * 			user to be checked
	 * @param branch
	 * 			branch to be checked
	 * @param file
	 * 			file to be checked, <tt>null</tt> means to check if the user can administer/delete the branch
	 * @return
	 * 			result of the check
	 */
	protected abstract CheckResult doCheckFile(User user, Branch branch, @Nullable String file);
	
	/**
	 * Check the gate keeper against specified user, branch and commit without considering enable flag. 
	 * This is typically used to determine whether or not to accept a push operation.
	 *
	 * @param user
	 * 			user to be checked
	 * @param branch
	 * 			branch to be checked
	 * @param commit
	 * 			commit to be checked
	 * @return
	 * 			result of the check
	 */
	protected abstract CheckResult doCheckCommit(User user, Branch branch, String commit);

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

	protected CheckResult pendingAndBlock(String reason, VoteEligibility voteEligibility) {
		return new PendingAndBlock(reason, voteEligibility);
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

	protected CheckResult pendingAndBlock(List<String> reasons, Collection<VoteEligibility> voteEligibilities) {
		return new PendingAndBlock(reasons, voteEligibilities);
	}

}
