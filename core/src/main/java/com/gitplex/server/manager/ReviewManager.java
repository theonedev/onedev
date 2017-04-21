package com.gitplex.server.manager;

import java.util.List;

import javax.annotation.Nullable;

import org.eclipse.jgit.lib.ObjectId;

import com.gitplex.server.model.Account;
import com.gitplex.server.model.Depot;
import com.gitplex.server.model.PullRequest;
import com.gitplex.server.model.PullRequestUpdate;
import com.gitplex.server.model.Review;
import com.gitplex.server.persistence.dao.EntityManager;
import com.gitplex.server.util.ReviewCheckStatus;

public interface ReviewManager extends EntityManager<Review> {

	Review find(Account user, PullRequestUpdate update);

	void review(PullRequest request, boolean approved, @Nullable String note);
	
	/**
	 * Find list of reviews ordered by review date for specified pull request.
	 * 
	 * @param request
	 * 			pull request to find review for
	 * @return
	 * 			list of reviews ordered by review date
	 */
	List<Review> findAll(PullRequest request);
	
	void delete(Account user, PullRequest request);
	
	ReviewCheckStatus checkRequest(PullRequest request);
	
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
	boolean canModify(Account user, Depot depot, String branch, String file);
	
	/**
	 * Check if specified user can push specified commit to specified ref.
	 *
	 * @param user
	 * 			user to be checked
	 * @param branchName
	 * 			branchName to be checked
	 * @param oldObjectId
	 * 			old object id of the ref
	 * @param newObjectId
	 * 			new object id of the ref
	 * @return
	 * 			result of the check
	 */
	boolean canPush(Account user, Depot depot, String branchName, ObjectId oldObjectId, ObjectId newObjectId);
	
}
