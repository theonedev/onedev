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

	private final PullRequestChangeManager pullRequestChangeManager;
	
	@Inject
	public DefaultPullRequestAssignmentManager(Dao dao, 
			PullRequestChangeManager pullRequestChangeManager) {
		super(dao);
		this.pullRequestChangeManager = pullRequestChangeManager;
	}

	@Transactional
	@Override
	public void addAssignee(PullRequestAssignment assignment) {
		save(assignment);
		
		PullRequest request = assignment.getRequest();
		request.getAssignments().add(assignment);
		
		PullRequestChange change = new PullRequestChange();
		change.setDate(new Date());
		change.setRequest(request);
		change.setData(new PullRequestAssigneeAddData(assignment.getUser().getDisplayName()));
		change.setUser(SecurityUtils.getUser());
		pullRequestChangeManager.save(change);
	}

	@Transactional
	@Override
	public void removeAssignee(PullRequestAssignment assignment) {
		delete(assignment);
		
		PullRequest request = assignment.getRequest();
		request.getAssignments().remove(assignment);
		
		PullRequestChange change = new PullRequestChange();
		change.setDate(new Date());
		change.setRequest(request);
		change.setData(new PullRequestAssigneeRemoveData(assignment.getUser().getDisplayName()));
		change.setUser(SecurityUtils.getUser());
		pullRequestChangeManager.save(change);
	}
		
}
