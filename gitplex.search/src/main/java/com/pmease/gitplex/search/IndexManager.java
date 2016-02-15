package com.pmease.gitplex.search;

import java.util.concurrent.Future;

import com.pmease.gitplex.core.listeners.RefListener;
import com.pmease.gitplex.core.listeners.DepotListener;
import com.pmease.gitplex.core.manager.IndexResult;
import com.pmease.gitplex.core.model.Depot;

public interface IndexManager extends DepotListener, RefListener {
	
	Future<IndexResult> index(Depot depot, String revision);
	
	boolean isIndexed(Depot depot, String revision);
	
}
