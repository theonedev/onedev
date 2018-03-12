package io.onedev.server.manager.impl;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.onedev.server.manager.IssueFieldManager;
import io.onedev.server.model.IssueField;
import io.onedev.server.persistence.dao.AbstractEntityManager;
import io.onedev.server.persistence.dao.Dao;

@Singleton
public class DefaultIssueFieldManager extends AbstractEntityManager<IssueField> 
		implements IssueFieldManager {

	@Inject
	public DefaultIssueFieldManager(Dao dao) {
		super(dao);
	}

}
