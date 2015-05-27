package com.pmease.gitplex.search;

import com.pmease.gitplex.core.listeners.RepositoryListener;
import com.pmease.gitplex.core.manager.IndexResult;
import com.pmease.gitplex.core.model.Repository;

public interface IndexManager extends RepositoryListener {
	IndexResult index(Repository repository, String revision);
}
