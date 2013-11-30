package com.pmease.gitop.core.manager.impl;

import java.util.Collection;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.hibernate.criterion.Restrictions;

import com.pmease.commons.hibernate.Sessional;
import com.pmease.commons.hibernate.dao.AbstractGenericDao;
import com.pmease.commons.hibernate.dao.GeneralDao;
import com.pmease.gitop.core.manager.BuildResultManager;
import com.pmease.gitop.model.BuildResult;

@Singleton
public class DefaultBuildResultManager extends AbstractGenericDao<BuildResult> 
		implements BuildResultManager {

	@Inject
	public DefaultBuildResultManager(GeneralDao generalDao) {
		super(generalDao);
	}

	@Sessional
	@Override
	public Collection<BuildResult> findBy(String commit) {
		return query(Restrictions.eq("commit", commit));
	}

	@Sessional
	@Override
	public BuildResult findBy(String commit, String configuration) {
		return find(Restrictions.eq("commit", commit), Restrictions.eq("configuration", configuration));
	}
	
}
