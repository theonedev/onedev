package com.gitplex.server.manager;

import java.util.Collection;

import javax.annotation.Nullable;

import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;

import com.gitplex.server.model.Project;
import com.gitplex.server.model.User;
import com.gitplex.server.persistence.dao.EntityManager;
import com.gitplex.server.util.facade.ProjectFacade;

public interface ProjectManager extends EntityManager<Project> {
	
	@Nullable Project find(String projectName);

	void fork(Project from, Project to);
	
	/**
	 * Save specified project. Note that oldName and oldUserId should not be 
	 * specified together, meaning that you should not rename and transfer 
	 * a project in a single call
	 * 
	 * @param project
	 * 			project to save
	 * @param oldName
	 * 			in case of rename, this parameter should hold the original name 
	 * 			when above project object is initially loaded to ensure database 
	 * 			integrity. Use <tt>null</tt> if original name does not exist, 
	 * 			or the name is not changed
	 */
	void save(Project project, @Nullable String oldName);
	
	Repository getRepository(Project project);
	
	Collection<ProjectFacade> getAccessibleProjects(@Nullable User user);
	
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
	boolean isModificationNeedsQualityCheck(User user, Project project, String branch, String file);
	
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
	boolean isPushNeedsQualityCheck(User user, Project project, String branchName, ObjectId oldObjectId, ObjectId newObjectId);
	
}
