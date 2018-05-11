package io.onedev.server.manager.impl;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.onedev.server.manager.IssueCommentManager;
import io.onedev.server.model.IssueComment;
import io.onedev.server.persistence.dao.AbstractEntityManager;
import io.onedev.server.persistence.dao.Dao;

@Singleton
public class DefaultIssueCommentManager extends AbstractEntityManager<IssueComment>
		implements IssueCommentManager {

	@Inject
	public DefaultIssueCommentManager(Dao dao) {
		super(dao);
	}

}
