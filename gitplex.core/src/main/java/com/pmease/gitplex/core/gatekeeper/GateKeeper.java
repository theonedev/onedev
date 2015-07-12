package com.pmease.gitplex.core.gatekeeper;

import java.io.Serializable;

import com.pmease.commons.editable.annotation.Editable;
import com.pmease.commons.util.trimmable.Trimmable;
import com.pmease.gitplex.core.gatekeeper.checkresult.CheckResult;
import com.pmease.gitplex.core.model.PullRequest;
import com.pmease.gitplex.core.model.User;
import com.pmease.gitplex.core.model.Repository;

@Editable(name="Misc")
public interface GateKeeper extends Trimmable, Serializable {
	
	String CATEGORY_COMMONLY_USED = "Commonly Used";
	
	String CATEGROY_CHECK_BRANCH = "Check Destination Branch";
	
	String CATEGROY_CHECK_FILES = "Check Touched Files";

	String CATEGROY_CHECK_REVIEW = "Check Reviews";

	String CATEGROY_CHECK_SUBMITTER = "Check Submitter";

	String CATEGROY_COMPOSITION = "Gate Keeper Composition";
	
	String CATEGROY_OTHERS = "Other Gate Keepers";
	
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
	 * 			file to be checked, pass <tt>null</tt> to check any file
	 * @return
	 * 			result of the check. 
	 */
	CheckResult checkFile(User user, Repository repository, String branch, String file);
	
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
	 * 			result of the check
	 */
	CheckResult checkCommit(User user, Repository repository, String branch, String commit);
	
	/**
	 * Check if specified user can push specified reference to specified repository. 
	 * 
	 * @param user
	 * 			user to be checked
	 * @param repository
	 * 			repository to be checked
	 * @param refName
	 * 			reference name to be checked
	 * @return
	 * 			result of the check
	 */
	CheckResult checkRef(User user, Repository repository, String refName);
	
	boolean isEnabled();
}
