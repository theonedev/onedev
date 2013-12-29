package com.pmease.gitop.model.gatekeeper;

import java.io.Serializable;

import com.pmease.commons.editable.annotation.Editable;
import com.pmease.commons.util.trimmable.Trimmable;
import com.pmease.gitop.model.Branch;
import com.pmease.gitop.model.PullRequest;
import com.pmease.gitop.model.User;
import com.pmease.gitop.model.gatekeeper.checkresult.CheckResult;

@Editable(name="Misc")
public interface GateKeeper extends Trimmable, Serializable {
	
	/**
	 * Check specified pull request.
	 * 
	 * @param request
	 * 			pull request to be checked. Note that <tt>request.getId()</tt>
	 * 			may return <tt>null</tt> to indicate a push operation, and in 
	 * 			this case, we should not invite any users to vote for the 
	 * 			request 
	 * @return
	 * 			check result
	 */
	CheckResult check(PullRequest request);
	
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
	 * 			check result
	 * 			
	 */
	CheckResult checkFile(User user, Branch branch, String file);

	/**
	 * Check if specified user can push specified commit to specified branch.
	 * 
	 * @param user
	 * 			user to be checked
	 * @param branch
	 * 			branch to be checked
	 * @param commit
	 * 			commit to be checked
	 * @return
	 * 			check result
	 * 			
	 */
	CheckResult checkCommit(User user, Branch branch, String commit);

	boolean isEnabled();
}
