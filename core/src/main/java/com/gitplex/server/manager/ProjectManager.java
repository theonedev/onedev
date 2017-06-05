package com.gitplex.server.manager;

import javax.annotation.Nullable;

import org.eclipse.jgit.lib.Repository;

import com.gitplex.server.model.Project;
import com.gitplex.server.persistence.dao.EntityManager;

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
	
}
