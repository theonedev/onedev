package com.pmease.gitop.core.manager.impl;

import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.hibernate.Query;

import com.google.common.collect.Maps;
import com.pmease.commons.hibernate.dao.AbstractGenericDao;
import com.pmease.commons.hibernate.dao.GeneralDao;
import com.pmease.gitop.core.manager.CommitCommentManager;
import com.pmease.gitop.model.CommitComment;
import com.pmease.gitop.model.Project;

@Singleton
public class DefaultCommitCommentManager extends AbstractGenericDao<CommitComment> 
		implements CommitCommentManager {

	@Inject
	public DefaultCommitCommentManager(GeneralDao generalDao) {
		super(generalDao);
	}

	@SuppressWarnings("unchecked")
	@Override
	public Map<String, Integer> getCommitCommentStats(Project project) {
		String sql = "SELECT c.commit, count(c.id) from CommitComment c "
					+ "WHERE project=:project "
				    + "GROUP BY c.commit";
		
		Query query = this.getSession().createQuery(sql);
		query.setParameter("project", project);
		List<Object[]> list = query.list();
		
		Map<String, Integer> map = Maps.newHashMap();
		for (Object[] each : list) {
			map.put((String) each[0], ((Long) each[1]).intValue());
		}
		
		return map;
	}
}
