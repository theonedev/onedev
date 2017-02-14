package com.gitplex.server.search;

import org.eclipse.jgit.lib.ObjectId;

import com.gitplex.server.core.entity.Depot;
import com.gitplex.server.core.manager.support.IndexResult;

public interface IndexManager {
	
	IndexResult index(Depot depot, ObjectId commit);
	
	void indexAsync(Depot depot, ObjectId commit);
	
	boolean isIndexed(Depot depot, ObjectId commit);
	
}
