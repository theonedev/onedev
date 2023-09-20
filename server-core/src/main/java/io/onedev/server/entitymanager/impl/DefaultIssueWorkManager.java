package io.onedev.server.entitymanager.impl;

import com.google.common.base.Preconditions;
import io.onedev.server.entitymanager.IssueWorkManager;
import io.onedev.server.model.IssueWork;
import io.onedev.server.persistence.dao.BaseEntityManager;
import io.onedev.server.persistence.dao.Dao;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class DefaultIssueWorkManager extends BaseEntityManager<IssueWork> implements IssueWorkManager {

	@Inject
    public DefaultIssueWorkManager(Dao dao) {
        super(dao);
    }

	@Override
	public void create(IssueWork work) {
		Preconditions.checkState(work.isNew());
		dao.persist(work);
	}

	@Override
	public void update(IssueWork work) {
		Preconditions.checkState(!work.isNew());
		dao.persist(work);
	}

}