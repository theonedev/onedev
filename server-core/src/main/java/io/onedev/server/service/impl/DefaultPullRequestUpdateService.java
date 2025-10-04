package io.onedev.server.service.impl;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.eclipse.jgit.lib.ObjectId;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

import com.google.common.base.Preconditions;

import io.onedev.server.event.ListenerRegistry;
import io.onedev.server.event.project.pullrequest.PullRequestUpdated;
import io.onedev.server.git.service.GitService;
import io.onedev.server.model.PullRequest;
import io.onedev.server.model.PullRequestUpdate;
import io.onedev.server.persistence.annotation.Sessional;
import io.onedev.server.persistence.annotation.Transactional;
import io.onedev.server.persistence.dao.EntityCriteria;
import io.onedev.server.service.ProjectService;
import io.onedev.server.service.PullRequestUpdateService;

@Singleton
public class DefaultPullRequestUpdateService extends BaseEntityService<PullRequestUpdate>
		implements PullRequestUpdateService {

	@Inject
	private ProjectService projectService;

	@Inject
	private GitService gitService;

	@Inject
	private ListenerRegistry listenerRegistry;

	@Transactional
	@Override
	public void create(PullRequestUpdate update) {
		Preconditions.checkState(update.isNew());
		dao.persist(update);
		PullRequest request = update.getRequest();
		if (!request.getTargetProject().equals(request.getSourceProject())) {
			if (projectService.hasLfsObjects(request.getSourceProject().getId())) {
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
			request.getAutoMerge().setEnabled(false);
			ObjectId mergeBase = gitService.getMergeBase(
					request.getTargetProject(), request.getTarget().getObjectId(), 
					request.getSourceProject(), request.getSource().getObjectId());
			if (mergeBase != null) {
				PullRequestUpdate update = new PullRequestUpdate();
				update.setRequest(request);
				update.setHeadCommitHash(request.getSource().getObjectName());
				update.setTargetHeadCommitHash(request.getTarget().getObjectName());
				request.getUpdates().add(update);
				create(update);

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
