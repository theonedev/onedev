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
import com.pmease.gitplex.core.entity.Depot;
import com.pmease.gitplex.core.entity.PullRequest;
import com.pmease.gitplex.core.entity.PullRequestUpdate;
import com.pmease.gitplex.core.event.pullrequest.PullRequestUpdated;
import com.pmease.gitplex.core.manager.PullRequestCommentManager;
import com.pmease.gitplex.core.manager.PullRequestUpdateManager;

@Singleton
public class DefaultPullRequestUpdateManager extends AbstractEntityManager<PullRequestUpdate> 
		implements PullRequestUpdateManager {
	
	private final ListenerRegistry listenerRegistry;
	
	@Inject
	public DefaultPullRequestUpdateManager(Dao dao, ListenerRegistry listenerRegistry,
			PullRequestCommentManager commentManager) {
		super(dao);
		
		this.listenerRegistry = listenerRegistry;
	}

	@Transactional
	@Override
	public void save(PullRequestUpdate update) {
		save(update, true);
	}
	
	@Transactional
	@Override
	public void save(PullRequestUpdate update, boolean notifyListeners) {
		PullRequest request = update.getRequest();
		
		String sourceHead = request.getSource().getObjectName();

		dao.persist(update);
		
		if (!request.getTargetDepot().equals(request.getSourceDepot())) {
			try {
				request.getTargetDepot().git().fetch()
						.setRemote(request.getSourceDepot().getDirectory().getAbsolutePath())
						.setRefSpecs(new RefSpec(request.getSourceRef() + ":" + update.getHeadRef()))
						.call();
			} catch (Exception e) {
				Throwables.propagate(e);
			}
		} else {
			RefUpdate refUpdate = request.getTargetDepot().updateRef(update.getHeadRef());
			refUpdate.setNewObjectId(ObjectId.fromString(sourceHead));
			GitUtils.updateRef(refUpdate);
		}

		if (notifyListeners)
			listenerRegistry.notify(new PullRequestUpdated(update));
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
