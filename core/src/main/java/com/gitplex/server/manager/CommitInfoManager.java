package com.gitplex.server.manager;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.eclipse.jgit.lib.ObjectId;

import com.gitplex.server.git.NameAndEmail;
import com.gitplex.server.model.Project;
import com.gitplex.server.util.facade.ProjectFacade;
import com.gitplex.server.util.facade.UserFacade;

public interface CommitInfoManager {
	
	List<String> getFiles(Project project);
	
	int getContributions(ProjectFacade project, UserFacade user, String path);
	
	int getCommitCount(Project project);
	
	List<NameAndEmail> getAuthors(Project project);
	
	List<NameAndEmail> getCommitters(Project project);
	
	/**
	 * Given an ancestor commit, get all its descendant commits including the ancestor commit itself. 
	 * The result might be incomplete if some commits have not be cached yet
	 *  
	 * @param project
	 * 			project to get descendant commits
	 * @param ancestor
	 * 			for which commit to get descendants
	 * @return
	 * 			descendant commits
	 */
	Set<ObjectId> getDescendants(Project project, ObjectId ancestor);
	
	/**
	 * Given a parent commit, get all its child commits. The result might be incomplete if some commits 
	 * have not be collected yet
	 *  
	 * @param project
	 * 			project to get descendant commits
	 * @param parent
	 * 			for which commit to get children
	 * @return
	 * 			child commits
	 */
	Set<ObjectId> getChildren(Project project, ObjectId parent);

	void cloneInfo(Project from, Project to);
	
	ObjectId getLastCommit(Project project);
	
	Collection<String> getPossibleHistoryPaths(Project project, String path);
	
}