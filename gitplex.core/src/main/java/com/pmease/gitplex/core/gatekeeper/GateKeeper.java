package com.pmease.gitplex.core.gatekeeper;

import java.io.Serializable;

import org.eclipse.jgit.lib.ObjectId;

import com.pmease.commons.util.trimmable.Trimmable;
import com.pmease.commons.wicket.editable.annotation.Editable;
import com.pmease.gitplex.core.gatekeeper.checkresult.CheckResult;
import com.pmease.gitplex.core.model.Depot;
import com.pmease.gitplex.core.model.PullRequest;
import com.pmease.gitplex.core.model.User;

@Editable(name="Misc")
public interface GateKeeper extends Trimmable, Serializable {
	
	String CATEGORY_BASIC = "Basic Gate Keepers";
	
	String CATEGORY_USER = "Check Approver/Submitter";
	
	String CATEGORY_COMPOSITION = "Gate Keeper Compositions";
	
	/**
	 * Check the gate keeper against specified request. This is typically used to determine 
	 * whether or not to accept a pull request. 
	 * 
	 * @param request
	 *			pull request to be checked
	 * @return
	 * 			result of the check
	 */
	CheckResult checkRequest(PullRequest request);

	/**
	 * Check if specified user can modify specified file in specified branch.
	 *
	 * @param user
	 * 			user to be checked
	 * @param branch
	 * 			branch to be checked
	 * @param file
	 * 			file to be checked
	 * @return
	 * 			result of the check. 
	 */
	CheckResult checkFile(User user, Depot depot, String branch, String file);
	
	/**
	 * Check if specified user can push specified commit to specified ref.
	 *
	 * @param user
	 * 			user to be checked
	 * @param refName
	 * 			refName to be checked
	 * @param oldCommitHash
	 * 			old commit of the ref
	 * @param newCommitHash
	 * 			new commit of the ref
	 * @return
	 * 			result of the check
	 */
	CheckResult checkPush(User user, Depot depot, String refName, ObjectId oldCommit, ObjectId newCommit);
	
	boolean isEnabled();
}
