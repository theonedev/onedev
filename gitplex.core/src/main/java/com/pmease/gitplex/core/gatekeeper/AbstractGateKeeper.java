package com.pmease.gitplex.core.gatekeeper;

import java.util.List;

import javax.annotation.Nullable;

import org.eclipse.jgit.lib.ObjectId;

import com.pmease.commons.wicket.editable.annotation.Editable;
import com.pmease.gitplex.core.entity.Depot;
import com.pmease.gitplex.core.entity.PullRequest;
import com.pmease.gitplex.core.entity.Account;
import com.pmease.gitplex.core.gatekeeper.checkresult.Blocking;
import com.pmease.gitplex.core.gatekeeper.checkresult.GateCheckResult;
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
	public GateCheckResult checkRequest(PullRequest request) {
		if (enabled)
			return doCheckRequest(request);
		else
			return ignored();
	}
	
	@Override
	public GateCheckResult checkFile(Account user, Depot depot, String branch, String file) {
		if (isEnabled())
			return doCheckFile(user, depot, branch, file);
		else
			return ignored();
	}
	
	@Override
	public GateCheckResult checkPush(Account user, Depot depot, String refName, ObjectId oldCommit, ObjectId newCommit) {
		if (isEnabled())
			return doCheckPush(user, depot, refName, oldCommit, newCommit);
		else
			return ignored();
	}

	/**
	 * Check gatekeeper against specified pull request without considering enable flag. This is 
	 * typically used to determine whether or not to accept a pull request. 
	 * 
	 * @param request
	 *			pull request to be checked
	 * @return
	 * 			result of the check
	 */
	protected abstract GateCheckResult doCheckRequest(PullRequest request);
	
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
	protected abstract GateCheckResult doCheckFile(Account user, Depot depot, String branch, @Nullable String file);

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
	protected abstract GateCheckResult doCheckPush(Account user, Depot depot, String refName, ObjectId oldCommit, ObjectId newCommit);

	protected GateCheckResult ignored() {
		return new Ignored();
	}

	protected GateCheckResult passed(List<String> reasons) {
		return new Passed(reasons);
	}
	
	protected GateCheckResult failed(List<String> reasons) {
		return new Failed(reasons);
	}

	protected GateCheckResult pending(List<String> reasons) {
		return new Pending(reasons);
	}

	protected GateCheckResult blocking(List<String> reasons) {
		return new Blocking(reasons);
	}

	@Override
	public void onDepotRename(Depot renamedDepot, String oldName) {
	}

	@Override
	public boolean onDepotTransfer(Depot depotDefiningGateKeeper, Depot transferredDepot, 
			Account originalAccount) {
		return false;
	}
	
	@Override
	public void onAccountRename(String oldName, String newName) {
	}

	@Override
	public boolean onAccountDelete(String accountName) {
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
	public boolean onTeamDelete(String teamName) {
		return false;
	}

	@Override
	public boolean onRefDelete(String refName) {
		return false;
	}
	
}
