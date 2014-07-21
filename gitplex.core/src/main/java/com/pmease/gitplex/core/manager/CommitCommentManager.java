package com.pmease.gitplex.core.manager;

import java.util.Map;

import com.google.inject.ImplementedBy;
import com.pmease.gitplex.core.manager.impl.DefaultCommitCommentManager;
import com.pmease.gitplex.core.model.Repository;

@ImplementedBy(DefaultCommitCommentManager.class)
public interface CommitCommentManager {
	
	Map<String, Integer> getCommitCommentStats(Repository repository);
	
}
