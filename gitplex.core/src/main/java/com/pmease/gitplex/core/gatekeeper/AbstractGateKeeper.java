package com.pmease.gitplex.core.gatekeeper;

import java.util.List;

import javax.annotation.Nullable;

import com.google.common.base.Preconditions;
import com.pmease.commons.wicket.editable.annotation.Editable;
import com.pmease.gitplex.core.gatekeeper.checkresult.Blocking;
import com.pmease.gitplex.core.gatekeeper.checkresult.CheckResult;
import com.pmease.gitplex.core.gatekeeper.checkresult.Failed;
import com.pmease.gitplex.core.gatekeeper.checkresult.Ignored;
import com.pmease.gitplex.core.gatekeeper.checkresult.Passed;
import com.pmease.gitplex.core.gatekeeper.checkresult.Pending;
import com.pmease.gitplex.core.model.PullRequest;
import com.pmease.gitplex.core.model.Repository;
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
	public CheckResult checkFile(User user, Repository repository, String branch, String file) {
		if (isEnabled())
			return doCheckFile(user, repository, branch, file);
		else
			return ignored();
	}
	
	@Override
	public CheckResult checkCommit(User user, Repository repository, String branch, String commit) {
		if (isEnabled())
			return doCheckCommit(user, repository, branch, commit);
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
	 * 			file to be checked
	 * @return
	 * 			result of the check
	 */
	protected abstract CheckResult doCheckFile(User user, Repository repository, String branch, String file);

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
	protected abstract CheckResult doCheckCommit(User user, Repository repository, String branch, String commit);

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

}
