package com.pmease.gitplex.core.manager;

import java.util.Date;
import java.util.Map;

import com.google.inject.ImplementedBy;
import com.pmease.gitplex.core.manager.impl.DefaultThreadVisitManager;
import com.pmease.gitplex.core.model.CommentPosition;
import com.pmease.gitplex.core.model.Repository;

@ImplementedBy(DefaultThreadVisitManager.class)
public interface ThreadVisitManager {
	
	/**
	 * Calculate map of visited comment position to date. 
	 * 
	 * @param repository
	 * 			repository to calculate visit map inside
	 * @param commit
	 * 			commit to calculate visit map for
	 * @return
	 * 			map of comment position to last visit date
	 */
	Map<CommentPosition, Date> calcVisitMap(Repository repository, String commit);
}
