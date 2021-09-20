package io.onedev.server.entitymanager.impl;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.hibernate.criterion.Restrictions;

import io.onedev.server.entitymanager.GitLfsLockManager;
import io.onedev.server.model.GitLfsLock;
import io.onedev.server.persistence.dao.BaseEntityManager;
import io.onedev.server.persistence.dao.Dao;
import io.onedev.server.persistence.dao.EntityCriteria;

@Singleton
public class DefaultGitLfsLockManager extends BaseEntityManager<GitLfsLock> implements GitLfsLockManager {

	@Inject
	public DefaultGitLfsLockManager(Dao dao) {
		super(dao);
	}

	@Override
	public GitLfsLock find(String path) {
		EntityCriteria<GitLfsLock> criteria = newCriteria();
		criteria.add(Restrictions.ilike(GitLfsLock.PROP_PATH, path));
		criteria.setCacheable(true);
		return find(criteria);
	}

}
