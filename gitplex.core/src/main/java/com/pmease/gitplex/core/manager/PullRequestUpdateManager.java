package com.pmease.gitplex.core.manager;

import java.util.List;

import javax.annotation.Nullable;

import com.pmease.commons.hibernate.dao.EntityManager;
import com.pmease.gitplex.core.entity.Depot;
import com.pmease.gitplex.core.entity.PullRequestUpdate;

public interface PullRequestUpdateManager extends EntityManager<PullRequestUpdate> {
	
	/**
	 * @param update
	 * 			update to be saved
	 * @param independent
	 * 			whether or not this update is an independent update. An independent update is 
	 * 			not created as result of other actions such as open and integrate
	 */
	void save(PullRequestUpdate update, boolean independent);
	
	@Nullable
	PullRequestUpdate find(String uuid);
	
	List<PullRequestUpdate> findAllAfter(Depot depot, @Nullable String updateUUID);
}
