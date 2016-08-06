package com.pmease.gitplex.core.manager.impl;

import java.util.Date;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.pmease.commons.hibernate.Transactional;
import com.pmease.commons.hibernate.dao.AbstractEntityManager;
import com.pmease.commons.hibernate.dao.Dao;
import com.pmease.commons.loader.Listen;
import com.pmease.gitplex.core.entity.PullRequestStatusChange;
import com.pmease.gitplex.core.event.pullrequest.PullRequestStatusChangeEvent;
import com.pmease.gitplex.core.manager.PullRequestStatusChangeManager;

@Singleton
public class DefaultPullRequestStatusChangeManager extends AbstractEntityManager<PullRequestStatusChange> 
		implements PullRequestStatusChangeManager {

	@Inject
	public DefaultPullRequestStatusChangeManager(Dao dao) {
		super(dao);
	}

	@Transactional
	@Listen
	public void on(PullRequestStatusChangeEvent event) {
		PullRequestStatusChange statusChange = new PullRequestStatusChange();
		statusChange.setRequest(event.getRequest());
		statusChange.setDate(new Date());
		statusChange.setEventType(event.getClass());
		statusChange.setUser(event.getUser());
		statusChange.setNote(event.getNote());
		save(statusChange);
	}

}
