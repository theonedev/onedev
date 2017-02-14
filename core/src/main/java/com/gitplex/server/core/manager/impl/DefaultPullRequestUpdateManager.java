package com.gitplex.server.core.manager.impl;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.RefUpdate;
import org.eclipse.jgit.transport.RefSpec;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

import com.google.common.base.Throwables;
import com.gitplex.commons.git.GitUtils;
import com.gitplex.commons.hibernate.Sessional;
import com.gitplex.commons.hibernate.Transactional;
import com.gitplex.commons.hibernate.dao.AbstractEntityManager;
import com.gitplex.commons.hibernate.dao.Dao;
import com.gitplex.commons.hibernate.dao.EntityCriteria;
import com.gitplex.calla.loader.ListenerRegistry;
import com.gitplex.commons.wicket.editable.EditableUtils;
import com.gitplex.server.core.entity.Depot;
import com.gitplex.server.core.entity.PullRequest;
import com.gitplex.server.core.entity.PullRequestUpdate;
import com.gitplex.server.core.entity.support.LastEvent;
import com.gitplex.server.core.event.pullrequest.PullRequestUpdated;
import com.gitplex.server.core.manager.PullRequestCommentManager;
import com.gitplex.server.core.manager.PullRequestManager;
import com.gitplex.server.core.manager.PullRequestUpdateManager;

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
