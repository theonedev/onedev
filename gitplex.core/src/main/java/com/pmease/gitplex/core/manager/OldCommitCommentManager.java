package com.pmease.gitplex.core.manager;

import java.util.Map;

import com.google.inject.ImplementedBy;
import com.pmease.gitplex.core.manager.impl.DefaultOldCommitCommentManager;
import com.pmease.gitplex.core.model.Repository;

@ImplementedBy(DefaultOldCommitCommentManager.class)
public interface OldCommitCommentManager {
	
	Map<String, Integer> getCommitCommentStats(Repository repository);
	
}
