package com.pmease.gitplex.core.manager;

import com.pmease.commons.hibernate.dao.EntityDao;
import com.pmease.gitplex.core.entity.PullRequestWatch;
import com.pmease.gitplex.core.extensionpoint.PullRequestListener;

public interface PullRequestWatchManager extends EntityDao<PullRequestWatch>, PullRequestListener {
	
}
