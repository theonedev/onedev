package com.gitplex.server.manager;

import java.util.List;

import javax.annotation.Nullable;

import org.eclipse.jgit.lib.ObjectId;

import com.gitplex.server.model.User;
import com.gitplex.server.model.Project;
import com.gitplex.server.model.PullRequest;
import com.gitplex.server.model.Review;
import com.gitplex.server.persistence.dao.EntityManager;
import com.gitplex.server.util.ReviewStatus;

public interface ReviewManager extends EntityManager<Review> {

	@Override
	void save(Review review);
	
	@Nullable
	Review find(PullRequest request, User user, String commit);
	
	/**
	 * Find list of reviews ordered by review date for specified pull request.
	 * 
	 * @param request
	 * 			pull request to find review for
	 * @return
	 * 			list of reviews ordered by review date
	 */
	List<Review> findAll(PullRequest request);
	
	void delete(User user, PullRequest request);
	
	ReviewStatus checkRequest(PullRequest request);
	
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
	boolean canModify(User user, Project project, String branch, String file);
	
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
	boolean canPush(User user, Project project, String branchName, ObjectId oldObjectId, ObjectId newObjectId);
	
}
