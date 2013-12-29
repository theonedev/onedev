package com.pmease.gitop.model.gatekeeper;

import java.util.Collection;
import java.util.List;

import javax.annotation.Nullable;

import com.google.common.base.Preconditions;
import com.pmease.commons.editable.annotation.Editable;
import com.pmease.commons.git.Commit;
import com.pmease.gitop.model.Branch;
import com.pmease.gitop.model.MergePrediction;
import com.pmease.gitop.model.Project;
import com.pmease.gitop.model.PullRequest;
import com.pmease.gitop.model.PullRequestUpdate;
import com.pmease.gitop.model.User;
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
	
	@Override
	public CheckResult checkFile(User user, Branch branch, String file) {
		return checkCommit(user, branch, Commit.ZERO_HASH + file);
	}
	
	
	@Override
	public CheckResult checkCommit(User user, Branch branch, String commit) {
		PullRequest request = new PullRequest();
		request.setTarget(branch);
		request.setSource(new Branch());
		request.getSource().setProject(new Project());
		request.getSource().getProject().setOwner(user);
		request.setTitle("Faked Pull Request");
		request.setMergePrediction(new MergePrediction(null, commit, commit));
		
		PullRequestUpdate update = new PullRequestUpdate();
		update.setRequest(request);
		update.setHeadCommit(request.getMergePrediction().getRequestHead());
		request.getUpdates().add(update);

		return check(request);
	}

	/**
	 * Check the gate keeper without considering enable flag. 
	 * 
	 * @param request
	 *			pull request to be checked. Gitop will create faked pull request in some
	 *			special cases explained below:
	 *			<ul>
	 *				<li> When determine whether or not a push operation should be accepted.
	 *				<li> When determine whether or not a branch can be deleted. In this 
	 *					case, the commit hash of the update will be {@link Commit#ZERO_HASH}.
	 *				<li> When determine whether or not a file can be touched. In this 
	 *					case, the commit hash of the update will be {@link Commit#ZERO_HASH}
	 *					concatenated with full path of the file.
	 *			</ul>  
	 *			For a faked pull request created in these special cases can be identified by 
	 *			checking whether or not <tt>request.getId()</tt> returns <tt>null</tt>
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
