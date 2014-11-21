package com.pmease.gitplex.core.manager;

import java.util.Map;

import com.pmease.gitplex.core.model.Repository;

public interface OldCommitCommentManager {
	
	Map<String, Integer> getCommitCommentStats(Repository repository);
	
}
