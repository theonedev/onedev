package io.onedev.server.manager.impl;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.onedev.server.manager.IssueActivityManager;
import io.onedev.server.model.IssueActivity;
import io.onedev.server.persistence.dao.AbstractEntityManager;
import io.onedev.server.persistence.dao.Dao;

@Singleton
public class DefaultIssueActivityManager extends AbstractEntityManager<IssueActivity>
		implements IssueActivityManager {

	@Inject
	public DefaultIssueActivityManager(Dao dao) {
		super(dao);
	}

}
