package com.pmease.gitop.model.gatekeeper;

import java.io.Serializable;

import javax.annotation.Nullable;

import com.pmease.commons.editable.annotation.Editable;
import com.pmease.commons.util.trimmable.Trimmable;
import com.pmease.gitop.model.Branch;
import com.pmease.gitop.model.PullRequest;
import com.pmease.gitop.model.User;
import com.pmease.gitop.model.gatekeeper.checkresult.CheckResult;

@Editable(name="Misc")
public interface GateKeeper extends Trimmable, Serializable {
	
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
	 * Check the gate keeper against specified user, branch and file. This is typically used 
	 * to determine whether or not to accept a file modification or branch deletion (when 
	 * file parameter is specified as <tt>null</tt>).
	 *
	 * @param user
	 * 			user to be checked
	 * @param branch
	 * 			branch to be checked
	 * @param file
	 * 			file to be checked, <tt>null</tt> means to check if the user can 
	 * 			administer/delete the branch
	 * @return
	 * 			result of the check. 
	 */
	CheckResult checkFile(User user, Branch branch, @Nullable String file);

	/**
	 * Check the gate keeper against specified user, branch and commit. This is typically used 
	 * to determine whether or not to accept a push operation.
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
	CheckResult checkCommit(User user, Branch branch, String commit);

	boolean isEnabled();
}
