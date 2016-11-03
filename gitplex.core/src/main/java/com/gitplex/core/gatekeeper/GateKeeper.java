package com.gitplex.core.gatekeeper;

import java.io.Serializable;

import javax.annotation.Nullable;

import org.eclipse.jgit.lib.ObjectId;

import com.gitplex.core.entity.Account;
import com.gitplex.core.entity.Depot;
import com.gitplex.core.entity.PullRequest;
import com.gitplex.core.gatekeeper.checkresult.GateCheckResult;
import com.gitplex.commons.wicket.editable.annotation.Editable;

@Editable(name="Misc")
public interface GateKeeper extends Serializable {
	
	String CATEGORY_BASIC = "Basic Gatekeepers";
	
	String CATEGORY_USER = "Check Approver/Submitter";
	
	String CATEGORY_COMPOSITION = "Gatekeeper Compositions";
	
	/**
	 * Check the gatekeeper against specified request. This is typically used to determine 
	 * whether or not to accept a pull request. 
	 * 
	 * @param request
	 *			pull request to be checked
	 * @return
	 * 			result of the check
	 */
	GateCheckResult checkRequest(PullRequest request);

	/**
	 * Check if specified user can modify specified file in specified branch.
	 *
	 * @param user
	 * 			user to be checked
	 * @param branch
	 * 			branch to be checked
	 * @param file
	 * 			file to be checked, use <tt>null</tt> to ignore checking of file, that is, 
	 * 			only check the branch
	 * @return
	 * 			result of the check. 
	 */
	GateCheckResult checkFile(Account user, Depot depot, String branch, @Nullable String file);
	
	/**
	 * Check if specified user can push specified commit to specified ref.
	 *
	 * @param user
	 * 			user to be checked
	 * @param refName
	 * 			refName to be checked
	 * @param oldObjectId
	 * 			old object id of the ref
	 * @param newObjectId
	 * 			new object id of the ref
	 * @return
	 * 			result of the check
	 */
	GateCheckResult checkPush(Account user, Depot depot, String refName, ObjectId oldObjectId, ObjectId newObjectId);
	
	void onDepotRename(Depot renamedDepot, String oldName);
	
	boolean onDepotDelete(Depot depot);
	
	boolean onDepotTransfer(Depot depotDefiningGateKeeper, Depot transferredDepot, Account originalAccount);
	
	void onTeamRename(String oldName, String newName);
	
	boolean onTeamDelete(String teamName);
	
	void onAccountRename(String oldName, String newName);
	
	boolean onAccountDelete(String accountName);
	
	boolean onRefDelete(String refName);
	
	boolean isEnabled();
}
