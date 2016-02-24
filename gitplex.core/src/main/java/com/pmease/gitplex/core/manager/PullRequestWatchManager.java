package com.pmease.gitplex.core.manager;

import com.pmease.commons.hibernate.dao.Dao;
import com.pmease.gitplex.core.extensionpoint.PullRequestListener;

public interface PullRequestWatchManager extends Dao, PullRequestListener {
	
}
