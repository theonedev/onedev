package com.turbodev.server.manager.impl;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.RefUpdate;
import org.eclipse.jgit.transport.RefSpec;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

import com.turbodev.launcher.loader.ListenerRegistry;
import com.google.common.base.Throwables;
import com.turbodev.server.event.pullrequest.PullRequestUpdated;
import com.turbodev.server.git.GitUtils;
import com.turbodev.server.manager.PullRequestCommentManager;
import com.turbodev.server.manager.PullRequestManager;
import com.turbodev.server.manager.PullRequestUpdateManager;
import com.turbodev.server.model.Project;
import com.turbodev.server.model.PullRequest;
import com.turbodev.server.model.PullRequestUpdate;
import com.turbodev.server.model.support.LastEvent;
import com.turbodev.server.persistence.annotation.Sessional;
import com.turbodev.server.persistence.annotation.Transactional;
import com.turbodev.server.persistence.dao.AbstractEntityManager;
import com.turbodev.server.persistence.dao.Dao;
import com.turbodev.server.persistence.dao.EntityCriteria;
import com.turbodev.server.util.editable.EditableUtils;

@Singleton
public class DefaultPullRequestUpdateManager extends AbstractEntityManager<PullRequestUpdate> 
		implements PullRequestUpdateManager {
	
	private final ListenerRegistry listenerRegistry;
	
	private final PullRequestManager pullRequestManager;
	
	@Inject
	public DefaultPullRequestUpdateManager(Dao dao, ListenerRegistry listenerRegistry,
			PullRequestCommentManager commentManager, PullRequestManager pullRequestManager) {
		super(dao);
		
		this.listenerRegistry = listenerRegistry;
		this.pullRequestManager = pullRequestManager;
	}

	@Transactional
	@Override
	public void save(PullRequestUpdate update) {
		save(update, true);
	}
	
	@Transactional
	@Override
	public void save(PullRequestUpdate update, boolean independent) {
		PullRequest request = update.getRequest();
		
		dao.persist(update);
		
		ObjectId updateHeadId = ObjectId.fromString(update.getHeadCommitHash());
		if (!request.getTargetProject().equals(request.getSourceProject())) {
			try {
				request.getTargetProject().git().fetch()
						.setRemote(request.getSourceProject().getGitDir().getAbsolutePath())
						.setRefSpecs(new RefSpec(GitUtils.branch2ref(request.getSourceBranch()) + ":" + update.getHeadRef()))
						.call();
				if (!request.getTargetProject().getObjectId(update.getHeadRef()).equals(updateHeadId)) {
					RefUpdate refUpdate = GitUtils.getRefUpdate(request.getTargetProject().getRepository(), 
							update.getHeadRef());
					refUpdate.setNewObjectId(updateHeadId);
					GitUtils.updateRef(refUpdate);
				}
			} catch (Exception e) {
				Throwables.propagate(e);
			}
		} else {
			RefUpdate refUpdate = GitUtils.getRefUpdate(request.getTargetProject().getRepository(), 
					update.getHeadRef());
			refUpdate.setNewObjectId(updateHeadId);
			GitUtils.updateRef(refUpdate);
		}

		request.setHeadCommitHash(update.getHeadCommitHash());
		pullRequestManager.save(request);
		
		if (independent) {
			PullRequestUpdated event = new PullRequestUpdated(update);
			listenerRegistry.post(event);
			
			LastEvent lastEvent = new LastEvent();
			lastEvent.setDate(event.getDate());
			lastEvent.setType(EditableUtils.getName(event.getClass()));
			request.setLastEvent(lastEvent);
		}
	}

	@Sessional
	@Override
	public PullRequestUpdate find(String uuid) {
		EntityCriteria<PullRequestUpdate> criteria = newCriteria();
		criteria.add(Restrictions.eq("uuid", uuid));
		return find(criteria);
	}

	@Sessional
	@Override
	public List<PullRequestUpdate> findAllAfter(Project project, String updateUUID, int count) {
		EntityCriteria<PullRequestUpdate> criteria = newCriteria();
		criteria.createCriteria("request").add(Restrictions.eq("targetProject", project));
		criteria.addOrder(Order.asc("id"));
		if (updateUUID != null) {
			PullRequestUpdate update = find(updateUUID);
			if (update != null) {
				criteria.add(Restrictions.gt("id", update.getId()));
			}
		}
		return findRange(criteria, 0, count);
	}

}
