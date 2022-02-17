package io.onedev.server.entitymanager.impl;

import java.util.Date;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.onedev.server.entitymanager.PullRequestAssignmentManager;
import io.onedev.server.entitymanager.PullRequestChangeManager;
import io.onedev.server.model.PullRequest;
import io.onedev.server.model.PullRequestAssignment;
import io.onedev.server.model.PullRequestChange;
import io.onedev.server.model.support.pullrequest.changedata.PullRequestAssigneeAddData;
import io.onedev.server.model.support.pullrequest.changedata.PullRequestAssigneeRemoveData;
import io.onedev.server.persistence.annotation.Transactional;
import io.onedev.server.persistence.dao.BaseEntityManager;
import io.onedev.server.persistence.dao.Dao;
import io.onedev.server.security.SecurityUtils;

@Singleton
public class DefaultPullRequestAssignmentManager extends BaseEntityManager<PullRequestAssignment> 
		implements PullRequestAssignmentManager {

	private final PullRequestChangeManager changeManager;
	
	@Inject
	public DefaultPullRequestAssignmentManager(Dao dao, PullRequestChangeManager changeManager) {
		super(dao);
		this.changeManager = changeManager;
	}

	@Transactional
	@Override
	public void save(PullRequestAssignment assignment) {
		super.save(assignment);
		
		PullRequest request = assignment.getRequest();		
		PullRequestChange change = new PullRequestChange();
		change.setDate(new Date());
		change.setRequest(request);
		change.setData(new PullRequestAssigneeAddData(assignment.getUser()));
		change.setUser(SecurityUtils.getUser());
		changeManager.save(change);
	}

	@Transactional
	@Override
	public void delete(PullRequestAssignment assignment) {
		super.delete(assignment);
		
		PullRequest request = assignment.getRequest();
		
		PullRequestChange change = new PullRequestChange();
		change.setDate(new Date());
		change.setRequest(request);
		change.setData(new PullRequestAssigneeRemoveData(assignment.getUser()));
		change.setUser(SecurityUtils.getUser());
		changeManager.save(change);
	}
		
}
