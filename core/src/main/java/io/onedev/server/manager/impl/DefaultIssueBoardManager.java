package io.onedev.server.manager.impl;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.onedev.server.manager.IssueBoardManager;
import io.onedev.server.model.IssueBoard;
import io.onedev.server.persistence.dao.AbstractEntityManager;
import io.onedev.server.persistence.dao.Dao;

@Singleton
public class DefaultIssueBoardManager extends AbstractEntityManager<IssueBoard>
		implements IssueBoardManager {

	@Inject
	public DefaultIssueBoardManager(Dao dao) {
		super(dao);
	}

}
