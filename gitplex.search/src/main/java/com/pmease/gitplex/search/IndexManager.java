package com.pmease.gitplex.search;

import java.util.concurrent.Future;

import org.eclipse.jgit.lib.ObjectId;

import com.pmease.gitplex.core.entity.Depot;
import com.pmease.gitplex.core.extensionpoint.DepotListener;
import com.pmease.gitplex.core.extensionpoint.RefListener;
import com.pmease.gitplex.core.manager.IndexResult;

public interface IndexManager extends DepotListener, RefListener {
	
	Future<IndexResult> index(Depot depot, ObjectId commit);
	
	boolean isIndexed(Depot depot, ObjectId commit);
	
}
