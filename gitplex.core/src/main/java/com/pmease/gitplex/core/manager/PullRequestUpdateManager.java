package com.pmease.gitplex.core.manager;

import java.util.List;

import javax.annotation.Nullable;

import com.pmease.commons.hibernate.dao.EntityManager;
import com.pmease.gitplex.core.entity.Depot;
import com.pmease.gitplex.core.entity.PullRequestUpdate;

public interface PullRequestUpdateManager extends EntityManager<PullRequestUpdate> {
	
	@Nullable
	PullRequestUpdate find(String uuid);
	
	List<PullRequestUpdate> findAllAfter(Depot depot, @Nullable String updateUUID);
}
