package com.pmease.gitplex.core.manager;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jgit.lib.ObjectId;

import com.pmease.commons.git.NameAndEmail;
import com.pmease.gitplex.core.model.Repository;

public interface AuxiliaryManager {
	
	void collect(Repository repository, String refName);
	
	List<String> getFiles(Repository repository);
	
	List<NameAndEmail> getContributors(Repository repository);
	
	Map<String, Map<NameAndEmail, Long>> getContributions(Repository repository, Set<String> files);

	/**
	 * Given an ancestor commit, get all its descendant commits known to this auxiliary. 
	 * The result might be incomplete if some commits have not be collected yet
	 *  
	 * @param repository
	 * 			repository to get descendant commits
	 * @param ancestor
	 * 			for which commit to get descendants
	 * @return
	 * 			descendant commits
	 */
	Set<ObjectId> getDescendants(Repository repository, ObjectId ancestor);
	
	/**
	 * Given a parent commit, get all its child commits known to this auxiliary. 
	 * The result might be incomplete if some commits have not be collected yet
	 *  
	 * @param repository
	 * 			repository to get descendant commits
	 * @param parent
	 * 			for which commit to get children
	 * @return
	 * 			child commits
	 */
	Set<ObjectId> getChildren(Repository repository, ObjectId parent);
}
