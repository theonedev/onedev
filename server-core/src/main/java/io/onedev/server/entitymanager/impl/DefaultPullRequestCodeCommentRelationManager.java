package io.onedev.server.entitymanager.impl;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.onedev.server.entitymanager.PullRequestCodeCommentRelationManager;
import io.onedev.server.model.PullRequestCodeCommentRelation;
import io.onedev.server.persistence.dao.BaseEntityManager;
import io.onedev.server.persistence.dao.Dao;

@Singleton
public class DefaultPullRequestCodeCommentRelationManager extends BaseEntityManager<PullRequestCodeCommentRelation> 
		implements PullRequestCodeCommentRelationManager {

	@Inject
	public DefaultPullRequestCodeCommentRelationManager(Dao dao) {
		super(dao);
	}

}
