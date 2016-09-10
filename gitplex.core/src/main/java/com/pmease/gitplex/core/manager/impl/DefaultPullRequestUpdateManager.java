package com.pmease.gitplex.core.manager.impl;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.RefUpdate;
import org.eclipse.jgit.transport.RefSpec;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

import com.google.common.base.Throwables;
import com.pmease.commons.git.GitUtils;
import com.pmease.commons.hibernate.Sessional;
import com.pmease.commons.hibernate.Transactional;
import com.pmease.commons.hibernate.dao.AbstractEntityManager;
import com.pmease.commons.hibernate.dao.Dao;
import com.pmease.commons.hibernate.dao.EntityCriteria;
import com.pmease.commons.loader.ListenerRegistry;
import com.pmease.commons.wicket.editable.EditableUtils;
import com.pmease.gitplex.core.entity.Depot;
import com.pmease.gitplex.core.entity.PullRequest;
import com.pmease.gitplex.core.entity.PullRequestUpdate;
import com.pmease.gitplex.core.entity.support.LastEvent;
import com.pmease.gitplex.core.event.pullrequest.PullRequestUpdated;
import com.pmease.gitplex.core.manager.PullRequestCommentManager;
import com.pmease.gitplex.core.manager.PullRequestManager;
import com.pmease.gitplex.core.manager.PullRequestUpdateManager;

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
		if (!request.getTargetDepot().equals(request.getSourceDepot())) {
			try {
				request.getTargetDepot().git().fetch()
						.setRemote(request.getSourceDepot().getDirectory().getAbsolutePath())
						.setRefSpecs(new RefSpec(GitUtils.branch2ref(request.getSourceBranch()) + ":" + update.getHeadRef()))
						.call();
				if (!request.getTargetDepot().getObjectId(update.getHeadRef()).equals(updateHeadId)) {
					RefUpdate refUpdate = request.getTargetDepot().updateRef(update.getHeadRef());
					refUpdate.setNewObjectId(updateHeadId);
					GitUtils.updateRef(refUpdate);
				}
			} catch (Exception e) {
				Throwables.propagate(e);
			}
		} else {
			RefUpdate refUpdate = request.getTargetDepot().updateRef(update.getHeadRef());
			refUpdate.setNewObjectId(updateHeadId);
			GitUtils.updateRef(refUpdate);
		}

		if (independent) {
			PullRequestUpdated event = new PullRequestUpdated(update);
			listenerRegistry.post(event);
			
			LastEvent lastEvent = new LastEvent();
			lastEvent.setDate(event.getDate());
			lastEvent.setType(EditableUtils.getName(event.getClass()));
			request.setLastEvent(lastEvent);
			pullRequestManager.save(request);
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
	public List<PullRequestUpdate> findAllAfter(Depot depot, String updateUUID) {
		EntityCriteria<PullRequestUpdate> criteria = newCriteria();
		criteria.createCriteria("request").add(Restrictions.eq("targetDepot", depot));
		criteria.addOrder(Order.asc("id"));
		if (updateUUID != null) {
			PullRequestUpdate update = find(updateUUID);
			if (update != null) {
				criteria.add(Restrictions.gt("id", update.getId()));
			}
		}
		return findAll(criteria);
	}

}
