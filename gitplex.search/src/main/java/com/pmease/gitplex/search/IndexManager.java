package com.pmease.gitplex.search;

import org.eclipse.jgit.lib.ObjectId;

import com.pmease.gitplex.core.entity.Depot;
import com.pmease.gitplex.core.manager.support.IndexResult;

public interface IndexManager {
	
	IndexResult index(Depot depot, ObjectId commit);
	
	void asyncIndex(Depot depot, ObjectId commit);
	
	boolean isIndexed(Depot depot, ObjectId commit);
	
}
