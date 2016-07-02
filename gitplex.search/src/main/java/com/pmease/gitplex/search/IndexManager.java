package com.pmease.gitplex.search;

import org.eclipse.jgit.lib.ObjectId;

import com.pmease.gitplex.core.entity.Depot;
import com.pmease.gitplex.core.listener.DepotListener;
import com.pmease.gitplex.core.listener.RefListener;
import com.pmease.gitplex.core.manager.support.IndexResult;

public interface IndexManager extends DepotListener, RefListener {
	
	IndexResult index(Depot depot, ObjectId commit);
	
	void requestToIndex(Depot depot, ObjectId commit);
	
	boolean isIndexed(Depot depot, ObjectId commit);
	
}
