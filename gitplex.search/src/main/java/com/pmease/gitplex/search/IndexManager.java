package com.pmease.gitplex.search;

import java.util.concurrent.Future;

import com.pmease.gitplex.core.entity.Depot;
import com.pmease.gitplex.core.extensionpoint.DepotListener;
import com.pmease.gitplex.core.extensionpoint.RefListener;
import com.pmease.gitplex.core.manager.IndexResult;

public interface IndexManager extends DepotListener, RefListener {
	
	Future<IndexResult> index(Depot depot, String revision);
	
	boolean isIndexed(Depot depot, String revision);
	
}
