package com.pmease.gitplex.core.manager;

import java.util.List;
import java.util.Map;

import org.eclipse.jgit.lib.ObjectId;

import com.pmease.gitplex.core.entity.Depot;

public interface CodeCommentInfoManager {
	
	void collect(Depot depot);
	
	/**
	 * Get comments over specified depot and commit
	 * 
	 * @return
	 * 			map of comment uuid to compare commit hash 
	 */
	Map<String, String> getComments(Depot depot, ObjectId commit);
	
	List<String> getCommentedFiles(Depot depot);
	
	void removeComment(Depot depot, ObjectId commit, String comment);
	
}