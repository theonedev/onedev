package io.onedev.server.entitymanager;

import java.util.Collection;
import java.util.List;

import javax.annotation.Nullable;

import org.apache.shiro.authz.Permission;
import org.eclipse.jgit.lib.Repository;

import io.onedev.server.model.Project;
import io.onedev.server.persistence.dao.EntityManager;
import io.onedev.server.search.entity.EntityCriteria;
import io.onedev.server.search.entity.EntityQuery;

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
	
	void create(Project project);
	
	void onDeleteBranch(Project project, String branchName);
	
	void deleteBranch(Project project, String branchName);
	
	void onDeleteTag(Project project, String tagName);
	
	void deleteTag(Project project, String tagName);
	
	Repository getRepository(Project project);
	
	Collection<Project> getPermittedProjects(Permission permission);
	
	List<Project> query(EntityQuery<Project> projecQuery, int firstResult, int maxResults);
	
	int count(EntityCriteria<Project> projectCriteria);
	
}
