package com.pmease.gitplex.core.manager;

import com.pmease.commons.hibernate.dao.Dao;
import com.pmease.gitplex.core.entity.PullRequestUpdate;

public interface PullRequestUpdateManager extends Dao {
	
	void save(PullRequestUpdate update, boolean notify);
	
	void delete(PullRequestUpdate update);
}
