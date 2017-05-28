package com.gitplex.server.search;

import org.eclipse.jgit.lib.ObjectId;

import com.gitplex.server.model.Project;

public interface IndexManager {
	
	void indexAsync(Project project, ObjectId commit);
	
	boolean isIndexed(Project project, ObjectId commit);
	
}
