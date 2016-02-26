package com.pmease.gitplex.core.gatekeeper;

import java.util.List;

import org.eclipse.jgit.lib.ObjectId;

import com.pmease.commons.wicket.editable.annotation.Editable;
import com.pmease.gitplex.core.entity.Depot;
import com.pmease.gitplex.core.entity.PullRequest;
import com.pmease.gitplex.core.entity.Team;
import com.pmease.gitplex.core.entity.User;
import com.pmease.gitplex.core.gatekeeper.checkresult.Blocking;
import com.pmease.gitplex.core.gatekeeper.checkresult.CheckResult;
import com.pmease.gitplex.core.gatekeeper.checkresult.Failed;
import com.pmease.gitplex.core.gatekeeper.checkresult.Ignored;
import com.pmease.gitplex.core.gatekeeper.checkresult.Passed;
import com.pmease.gitplex.core.gatekeeper.checkresult.Pending;

@Editable
public abstract class AbstractGateKeeper implements GateKeeper {

	private static final long serialVersionUID = 1L;
	
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
	public CheckResult checkFile(User user, Depot depot, String branch, String file) {
		if (isEnabled())
			return doCheckFile(user, depot, branch, file);
		else
			return ignored();
	}
	
	@Override
	public CheckResult checkPush(User user, Depot depot, String refName, ObjectId oldCommit, ObjectId newCommit) {
		if (isEnabled())
			return doCheckPush(user, depot, refName, oldCommit, newCommit);
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
	 * 			file to be checked
	 * @return
	 * 			result of the check
	 */
	protected abstract CheckResult doCheckFile(User user, Depot depot, String branch, String file);

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
	protected abstract CheckResult doCheckPush(User user, Depot depot, String refName, ObjectId oldCommit, ObjectId newCommit);

	protected CheckResult ignored() {
		return new Ignored();
	}

	protected CheckResult passed(List<String> reasons) {
		return new Passed(reasons);
	}
	
	protected CheckResult failed(List<String> reasons) {
		return new Failed(reasons);
	}

	protected CheckResult pending(List<String> reasons) {
		return new Pending(reasons);
	}

	protected CheckResult blocking(List<String> reasons) {
		return new Blocking(reasons);
	}

	@Override
	public void onDepotRename(User depotOwner, String oldName, String newName) {
	}

	@Override
	public void onUserRename(String oldName, String newName) {
	}

	@Override
	public boolean onUserDelete(User user) {
		return false;
	}

	@Override
	public boolean onDepotDelete(Depot depot) {
		return false;
	}

	@Override
	public void onTeamRename(String oldName, String newName) {
	}

	@Override
	public boolean onTeamDelete(Team team) {
		return false;
	}

	@Override
	public boolean onRefDelete(String refName) {
		return false;
	}
	
}
