package com.pmease.gitplex.core.manager;

import com.pmease.commons.hibernate.dao.EntityDao;
import com.pmease.gitplex.core.entity.PullRequestUpdate;

public interface PullRequestUpdateManager extends EntityDao<PullRequestUpdate> {
	
	void save(PullRequestUpdate update, boolean notify);
	
	void delete(PullRequestUpdate update);
}
