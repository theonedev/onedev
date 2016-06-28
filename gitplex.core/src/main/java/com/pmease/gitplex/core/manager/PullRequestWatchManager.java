package com.pmease.gitplex.core.manager;

import com.pmease.commons.hibernate.dao.EntityManager;
import com.pmease.gitplex.core.entity.PullRequestWatch;
import com.pmease.gitplex.core.listener.PullRequestListener;

public interface PullRequestWatchManager extends EntityManager<PullRequestWatch>, PullRequestListener {
	
}
