package com.pmease.gitplex.core.manager.impl;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.hibernate.criterion.Restrictions;

import com.pmease.commons.hibernate.Sessional;
import com.pmease.commons.hibernate.dao.Dao;
import com.pmease.commons.hibernate.dao.EntityCriteria;
import com.pmease.gitplex.core.manager.ThreadVisitManager;
import com.pmease.gitplex.core.model.CommentPosition;
import com.pmease.gitplex.core.model.Repository;
import com.pmease.gitplex.core.model.ThreadVisit;

@Singleton
public class DefaultThreadVisitManager implements ThreadVisitManager {

	private final Dao dao;
	
	@Inject
	public DefaultThreadVisitManager(Dao dao) {
		this.dao = dao;
	}

	@Sessional
	@Override
	public Map<CommentPosition, Date> calcVisitMap(Repository repository, String commit) {
		Map<CommentPosition, Date> visitMap = new HashMap<>(); 
		List<ThreadVisit> visits = dao.query(EntityCriteria.of(ThreadVisit.class)
				.add(Restrictions.eq("repository", repository))
				.add(Restrictions.eq("commit", commit)), 0, 0);
		for (ThreadVisit visit: visits)	
			visitMap.put(visit.getPosition(), visit.getDate());
		
		return visitMap;
	}

}
