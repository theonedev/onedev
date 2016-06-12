package com.pmease.gitplex.core.manager;

import java.util.List;

import javax.annotation.Nullable;

import com.pmease.commons.hibernate.dao.EntityDao;
import com.pmease.gitplex.core.entity.Depot;
import com.pmease.gitplex.core.entity.PullRequestUpdate;

public interface PullRequestUpdateManager extends EntityDao<PullRequestUpdate> {
	
	void save(PullRequestUpdate update, boolean notify);
	
	@Nullable
	PullRequestUpdate find(String uuid);
	
	List<PullRequestUpdate> queryAfter(Depot depot, @Nullable String updateUUID);
}
