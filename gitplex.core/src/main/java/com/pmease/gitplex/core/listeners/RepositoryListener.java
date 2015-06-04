package com.pmease.gitplex.core.listeners;

import com.pmease.commons.loader.ExtensionPoint;
import com.pmease.gitplex.core.model.Repository;

@ExtensionPoint
public interface RepositoryListener {
	
	void repositoryRemoved(Repository repository);
	
	void commitReceived(Repository repository, String commitHash);
}
