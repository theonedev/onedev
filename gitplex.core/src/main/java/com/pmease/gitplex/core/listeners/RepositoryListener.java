package com.pmease.gitplex.core.listeners;

import javax.annotation.Nullable;

import com.pmease.commons.loader.ExtensionPoint;
import com.pmease.gitplex.core.model.Repository;

@ExtensionPoint
public interface RepositoryListener {
	
	void beforeDelete(Repository repository);
	
	void afterDelete(Repository repository);
	
	void onRefUpdate(Repository repository, String refName, @Nullable String newCommitHash);
}
