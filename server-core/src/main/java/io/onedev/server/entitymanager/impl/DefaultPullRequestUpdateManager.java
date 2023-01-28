package io.onedev.server.entitymanager.impl;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.eclipse.jgit.lib.ObjectId;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

import io.onedev.server.entitymanager.PullRequestCommentManager;
import io.onedev.server.entitymanager.PullRequestUpdateManager;
import io.onedev.server.event.ListenerRegistry;
import io.onedev.server.event.project.pullrequest.PullRequestUpdated;
import io.onedev.server.git.service.GitService;
import io.onedev.server.model.PullRequest;
import io.onedev.server.model.PullRequestUpdate;
import io.onedev.server.persistence.annotation.Sessional;
import io.onedev.server.persistence.annotation.Transactional;
import io.onedev.server.persistence.dao.BaseEntityManager;
import io.onedev.server.persistence.dao.Dao;
import io.onedev.server.persistence.dao.EntityCriteria;

@Singleton
public class DefaultPullRequestUpdateManager extends BaseEntityManager<PullRequestUpdate> 
		implements PullRequestUpdateManager {
	
	private final GitService gitService;
	
	private final ListenerRegistry listenerRegistry;
	
	@Inject
	public DefaultPullRequestUpdateManager(Dao dao, ListenerRegistry listenerRegistry,
			PullRequestCommentManager commentManager, GitService gitService) {
		super(dao);
		
		this.gitService = gitService;
		this.listenerRegistry = listenerRegistry;
	}

	@Transactional
	@Override
	public void save(PullRequestUpdate update) {
		super.save(update);
		PullRequest request = update.getRequest();
		if (!request.getTargetProject().equals(request.getSourceProject())) {
			if (request.getTargetProject().findPullRequestWithLFS()) {
				gitService.pushLfsObjects(
						request.getSourceProject(), request.getSourceRef(),
						request.getTargetProject(), update.getHeadRef(),
						ObjectId.fromString(update.getHeadCommitHash()));
			}
			gitService.push(request.getSourceProject(), update.getHeadCommitHash(), 
					request.getTargetProject(), update.getHeadRef());
		} else {
			ObjectId headCommitId = ObjectId.fromString(update.getHeadCommitHash());
			gitService.updateRef(request.getTargetProject(), update.getHeadRef(), headCommitId, null);
		}
	}

	@Transactional
	@Override
	public void checkUpdate(PullRequest request) {
		if (!request.getLatestUpdate().getHeadCommitHash().equals(request.getSource().getObjectName())) {
			ObjectId mergeBase = gitService.getMergeBase(
					request.getTargetProject(), request.getTarget().getObjectId(), 
					request.getSourceProject(), request.getSource().getObjectId());
			if (mergeBase != null) {
				PullRequestUpdate update = new PullRequestUpdate();
				update.setRequest(request);
				update.setHeadCommitHash(request.getSource().getObjectName());
				update.setTargetHeadCommitHash(request.getTarget().getObjectName());
				request.getUpdates().add(update);
				save(update);

				gitService.updateRef(request.getTargetProject(), request.getHeadRef(), 
						ObjectId.fromString(request.getLatestUpdate().getHeadCommitHash()), null);
				
				listenerRegistry.post(new PullRequestUpdated(update));
			}
		}
	}
	
	@Sessional
	@Override
	public List<PullRequestUpdate> queryAfter(Long projectId, Long afterUpdateId, int count) {
		EntityCriteria<PullRequestUpdate> criteria = newCriteria();
		criteria.createCriteria("request").add(Restrictions.eq("targetProject.id", projectId));
		criteria.add(Restrictions.gt("id", afterUpdateId));
		criteria.addOrder(Order.asc("id"));
		return query(criteria, 0, count);
	}

}
