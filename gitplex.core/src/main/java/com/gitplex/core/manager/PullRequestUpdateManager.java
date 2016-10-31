package com.gitplex.core.manager;

import java.util.List;

import javax.annotation.Nullable;

import com.gitplex.core.entity.Depot;
import com.gitplex.core.entity.PullRequestUpdate;
import com.gitplex.commons.hibernate.dao.EntityManager;

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
