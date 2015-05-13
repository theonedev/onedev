package com.pmease.gitplex.core.manager;

import java.util.Collection;

import com.pmease.gitplex.core.comment.Comment;
import com.pmease.gitplex.core.model.Branch;
import com.pmease.gitplex.core.model.PullRequest;
import com.pmease.gitplex.core.model.Review;
import com.pmease.gitplex.core.model.Repository;
import com.pmease.gitplex.core.model.User;
import com.pmease.gitplex.core.permission.operation.GeneralOperation;

public interface AuthorizationManager {
	
	Collection<User> listAuthorizedUsers(Repository repository, GeneralOperation operation);
	
	boolean canModifyRequest(PullRequest request);
	
	boolean canModifyReview(Review review);
	
	boolean canModifyComment(Comment comment);

	/**
	 * Check if current user can modify or delete specified branch with gate keeper considered. 
	 * 
	 * @param branch
	 * 			branch to check
	 * @return
	 * 			true if able to modify/delete specified branch
	 */
	boolean canModifyBranch(Branch branch);
	
	/**
	 * Check if current user can create specified branch in specified repository.
	 * 
	 * @param repository
	 * 			repository to be checked
	 * @param branchName
	 * 			branch to be created
	 * @return
	 * 			true if able to create specified branch in specified repository
	 */
	boolean canCreateBranch(Repository repository, String branchName);
	
	boolean canManageAccount(User account);
	
}
