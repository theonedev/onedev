package io.onedev.server.entitymanager.impl;

import java.util.Date;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.onedev.server.entitymanager.PullRequestChangeManager;
import io.onedev.server.event.ListenerRegistry;
import io.onedev.server.event.project.pullrequest.PullRequestChanged;
import io.onedev.server.model.PullRequest;
import io.onedev.server.model.PullRequestChange;
import io.onedev.server.model.PullRequestComment;
import io.onedev.server.model.support.pullrequest.MergeStrategy;
import io.onedev.server.model.support.pullrequest.changedata.PullRequestMergeStrategyChangeData;
import io.onedev.server.model.support.pullrequest.changedata.PullRequestTargetBranchChangeData;
import io.onedev.server.model.support.pullrequest.changedata.PullRequestTitleChangeData;
import io.onedev.server.persistence.annotation.Transactional;
import io.onedev.server.persistence.dao.BaseEntityManager;
import io.onedev.server.persistence.dao.Dao;
import io.onedev.server.security.SecurityUtils;

@Singleton
public class DefaultPullRequestChangeManager extends BaseEntityManager<PullRequestChange> 
		implements PullRequestChangeManager {

	private final ListenerRegistry listenerRegistry;
	
	@Inject
	public DefaultPullRequestChangeManager(Dao dao, ListenerRegistry listenerRegistry) {
		super(dao);
		this.listenerRegistry = listenerRegistry;
	}

	@Transactional
	@Override
	public void save(PullRequestChange change, String note) {
		dao.persist(change);
		
		if (note != null) {
			PullRequestComment comment = new PullRequestComment();
			comment.setContent(note);
			comment.setUser(change.getUser());
			comment.setRequest(change.getRequest());
			comment.setDate(change.getDate());
			dao.persist(comment);
			comment.getRequest().setCommentCount(comment.getRequest().getCommentCount()+1);
		}
		
		listenerRegistry.post(new PullRequestChanged(change, note));
	}
	
	@Override
	public void save(PullRequestChange change) {
		save(change, null);
	}
	
	@Transactional
	@Override
	public void changeMergeStrategy(PullRequest request, MergeStrategy mergeStrategy) {
		PullRequestChange change = new PullRequestChange();
		change.setDate(new Date());
		change.setRequest(request);
		change.setData(new PullRequestMergeStrategyChangeData(request.getMergeStrategy(), mergeStrategy));
		change.setUser(SecurityUtils.getUser());
		save(change);
		request.setMergeStrategy(mergeStrategy);
	}

	@Transactional
	@Override
	public void changeTitle(PullRequest request, String title) {
		String prevTitle = request.getTitle();
		if (!title.equals(prevTitle)) {
			request.setTitle(title);
			
			PullRequestChange change = new PullRequestChange();
			change.setDate(new Date());
			change.setRequest(request);
			change.setData(new PullRequestTitleChangeData(prevTitle, title));
			change.setUser(SecurityUtils.getUser());
			save(change);
			
			dao.persist(request);
		}
	}

	@Transactional
	@Override
	public void changeTargetBranch(PullRequest request, String targetBranch) {
		String prevTargetBranch = request.getTargetBranch();
		if (!targetBranch.equals(prevTargetBranch)) {
			request.setTargetBranch(targetBranch);
			
			PullRequestChange change = new PullRequestChange();
			change.setDate(new Date());
			change.setRequest(request);
			change.setData(new PullRequestTargetBranchChangeData(prevTargetBranch, targetBranch));
			change.setUser(SecurityUtils.getUser());
			save(change);
			
			dao.persist(request);
		}
	}

}
