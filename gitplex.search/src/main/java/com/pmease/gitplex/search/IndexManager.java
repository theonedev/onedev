package com.pmease.gitplex.search;

import java.util.concurrent.Future;

import com.pmease.gitplex.core.listeners.RefListener;
import com.pmease.gitplex.core.listeners.RepositoryListener;
import com.pmease.gitplex.core.manager.IndexResult;
import com.pmease.gitplex.core.model.Repository;

public interface IndexManager extends RepositoryListener, RefListener {
	
	Future<IndexResult> index(Repository repository, String revision);
	
	boolean isIndexed(Repository repository, String revision);
	
}
