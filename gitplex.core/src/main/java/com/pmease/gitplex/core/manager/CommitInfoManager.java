package com.pmease.gitplex.core.manager;

import java.util.List;
import java.util.Set;

import org.eclipse.jgit.lib.ObjectId;

import com.pmease.commons.git.NameAndEmail;
import com.pmease.gitplex.core.entity.Depot;

public interface CommitInfoManager {
	
	void collect(Depot depot);
	
	void collect(Depot depot, ObjectId commit);
	
	List<String> getFiles(Depot depot);
	
	int getCommitCount(Depot depot);
	
	List<NameAndEmail> getAuthors(Depot depot);
	
	List<NameAndEmail> getCommitters(Depot depot);
	
	/**
	 * Given an ancestor commit, get all its descendant commits. The result might be incomplete 
	 * if some commits have not be cached yet
	 *  
	 * @param depot
	 * 			repository to get descendant commits
	 * @param ancestor
	 * 			for which commit to get descendants
	 * @return
	 * 			descendant commits
	 */
	Set<ObjectId> getDescendants(Depot depot, ObjectId ancestor);
	
	/**
	 * Given a parent commit, get all its child commits. The result might be incomplete if some commits 
	 * have not be collected yet
	 *  
	 * @param depot
	 * 			repository to get descendant commits
	 * @param parent
	 * 			for which commit to get children
	 * @return
	 * 			child commits
	 */
	Set<ObjectId> getChildren(Depot depot, ObjectId parent);
}
