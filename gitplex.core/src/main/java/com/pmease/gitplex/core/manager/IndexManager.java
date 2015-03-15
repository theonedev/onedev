package com.pmease.gitplex.core.manager;

import com.pmease.gitplex.core.model.Repository;

public interface IndexManager {
	void index(Repository repository, String commit);
	
	String getAnalyzersVersion(Repository repository, String commitHash);
}
