package com.pmease.gitplex.core.gatekeeper;

import java.util.Collection;
import java.util.List;

import javax.annotation.Nullable;

import com.google.common.base.Preconditions;
import com.pmease.commons.editable.annotation.Editable;
import com.pmease.gitplex.core.gatekeeper.checkresult.Approved;
import com.pmease.gitplex.core.gatekeeper.checkresult.CheckResult;
import com.pmease.gitplex.core.gatekeeper.checkresult.Disapproved;
import com.pmease.gitplex.core.gatekeeper.checkresult.Ignored;
import com.pmease.gitplex.core.gatekeeper.checkresult.Pending;
import com.pmease.gitplex.core.gatekeeper.checkresult.PendingAndBlock;
import com.pmease.gitplex.core.gatekeeper.voteeligibility.VoteEligibility;
import com.pmease.gitplex.core.model.Branch;
import com.pmease.gitplex.core.model.Repository;
import com.pmease.gitplex.core.model.PullRequest;
import com.pmease.gitplex.core.model.User;

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
			return ignored();
	}
	
	@Override
	public CheckResult checkFile(User user, Branch branch, String file) {
		if (isEnabled())
			return doCheckFile(user, branch, file);
		else
			return ignored();
	}
	
	public CheckResult checkBranch(User user, Branch branch) {
		if (isEnabled())
			return doCheckFile(user, branch, null);
		else
			return ignored();
	}
	
	@Override
	public CheckResult checkCommit(User user, Branch branch, String commit) {
		if (isEnabled())
			return doCheckCommit(user, branch, commit);
		else
			return ignored();
	}

	@Override
	public CheckResult checkRef(User user, Repository repository, String refName) {
		if (isEnabled())
			return doCheckRef(user, repository, refName);
		else
			return ignored();
	}
	
	/**
	 * Check gate keeper against specified pull request without considering enable flag. This is 
	 * typically used to determine whether or not to accept a pull request. 
	 * 
	 * @param request
	 *			pull request to be checked
	 * @return
	 * 			result of the check
	 */
	protected abstract CheckResult doCheckRequest(PullRequest request);
	
	/**
	 * Check if specified user can modify specified file in specified branch, without considering enable flag.
	 *
	 * @param user
	 * 			user to be checked
	 * @param branch
	 * 			branch to be checked
	 * @param file
	 * 			file to be checked, pass <tt>null</tt> to check for any file
	 * @return
	 * 			result of the check
	 */
	protected abstract CheckResult doCheckFile(User user, Branch branch, @Nullable String file);

	/**
	 * Check if specified user can push specified commit to specified branch, without considering enable flag.
	 * 
	 * @param user
	 *			user to be checked 	
	 * @param branch
	 * 			branch to be checked
	 * @param commit
	 * 			commit to be checked
	 * @return
	 * 			result of the check
	 */
	protected abstract CheckResult doCheckCommit(User user, Branch branch, String commit);

	/**
	 * Check if specified user can create/delete specified reference in specified repository, 
	 * without considering enable flag.
	 * 
	 * @param user
	 *			user to be checked 	
	 * @param repository
	 * 			repository to be checked
	 * @param refName
	 * 			reference name to be checked
	 * @return
	 * 			result of the check
	 */
	protected abstract CheckResult doCheckRef(User user, Repository repository, String refName);
	
	@Override
	public final Object trim(@Nullable Object context) {
		Preconditions.checkArgument(context instanceof Repository);
		return trim((Repository)context);
	}
	
	protected GateKeeper trim(Repository repository) {
		return this;
	}

	protected CheckResult approved(String reason) {
		return new Approved(reason);
	}

	protected CheckResult disapproved(String reason) {
		return new Disapproved(reason);
	}

	protected CheckResult pending(String reason, VoteEligibility voteEligibility) {
		return new Pending(reason, voteEligibility);
	}

	protected CheckResult pendingAndBlock(String reason, VoteEligibility voteEligibility) {
		return new PendingAndBlock(reason, voteEligibility);
	}

	protected CheckResult ignored() {
		return new Ignored();
	}

	protected CheckResult approved(List<String> reasons) {
		return new Approved(reasons);
	}
	
	protected CheckResult disapproved(List<String> reasons) {
		return new Disapproved(reasons);
	}

	protected CheckResult pending(List<String> reasons, Collection<VoteEligibility> voteEligibilies) {
		return new Pending(reasons, voteEligibilies);
	}

	protected CheckResult pendingAndBlock(List<String> reasons, Collection<VoteEligibility> voteEligibilities) {
		return new PendingAndBlock(reasons, voteEligibilities);
	}

}
