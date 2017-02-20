package com.gitplex.server.manager;

import java.util.List;
import java.util.Map;

import org.eclipse.jgit.lib.ObjectId;

import com.gitplex.server.model.Depot;
import com.gitplex.server.model.support.CompareContext;

public interface CodeCommentInfoManager {
	
	void collect(Depot depot);
	
	/**
	 * Get comments over specified depot and commit
	 * 
	 * @return
	 * 			map of comment uuid to compare context 
	 */
	Map<String, CompareContext> getComments(Depot depot, ObjectId commit);
	
	List<String> getCommentedFiles(Depot depot);
	
	void removeComment(Depot depot, ObjectId commit, String comment);
	
}