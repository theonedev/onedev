package com.pmease.gitplex.core.manager.impl;

import java.util.List;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

import com.pmease.commons.hibernate.Sessional;
import com.pmease.commons.hibernate.Transactional;
import com.pmease.commons.hibernate.dao.AbstractEntityDao;
import com.pmease.commons.hibernate.dao.Dao;
import com.pmease.commons.hibernate.dao.EntityCriteria;
import com.pmease.commons.util.FileUtils;
import com.pmease.gitplex.core.entity.Depot;
import com.pmease.gitplex.core.entity.PullRequest;
import com.pmease.gitplex.core.entity.PullRequestUpdate;
import com.pmease.gitplex.core.listener.PullRequestListener;
import com.pmease.gitplex.core.listener.PullRequestUpdateListener;
import com.pmease.gitplex.core.manager.PullRequestCommentManager;
import com.pmease.gitplex.core.manager.PullRequestUpdateManager;
import com.pmease.gitplex.core.manager.StorageManager;

@Singleton
public class DefaultPullRequestUpdateManager extends AbstractEntityDao<PullRequestUpdate> implements PullRequestUpdateManager {
	
	private final StorageManager storageManager;
	
	private final Set<PullRequestListener> pullRequestListeners;
	
	private final Provider<Set<PullRequestUpdateListener>> pullRequestUpdateListenersProvider;
	
	@Inject
	public DefaultPullRequestUpdateManager(Dao dao, StorageManager storageManager, 
			Set<PullRequestListener> pullRequestListeners, 
			Provider<Set<PullRequestUpdateListener>> pullRequestUpdateListenersProvider,
			PullRequestCommentManager commentManager) {
		super(dao);
		
		this.storageManager = storageManager;
		this.pullRequestListeners = pullRequestListeners;
		this.pullRequestUpdateListenersProvider = pullRequestUpdateListenersProvider;
	}

	@Transactional
	@Override
	public void save(PullRequestUpdate update, boolean notify) {
		for (PullRequestUpdateListener listener: pullRequestUpdateListenersProvider.get())
			listener.onSaveUpdate(update);
		
		persist(update);
		
		FileUtils.cleanDir(storageManager.getCacheDir(update));

		PullRequest request = update.getRequest();
		String sourceHead = request.getSource().getObjectName();

		if (!request.getTargetDepot().equals(request.getSourceDepot())) {
			request.getTargetDepot().git().fetch(
					request.getSourceDepot().git(), 
					"+" + request.getSourceRef() + ":" + update.getHeadRef()); 
		} else {
			request.getTargetDepot().git().updateRef(update.getHeadRef(), 
					sourceHead, null, null);
		}
		
		if (notify) { 
			for (PullRequestListener listener: pullRequestListeners)
				listener.onUpdateRequest(update);
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
	public List<PullRequestUpdate> queryAfter(Depot depot, String updateUUID) {
		EntityCriteria<PullRequestUpdate> criteria = newCriteria();
		criteria.createCriteria("request").add(Restrictions.eq("targetDepot", depot));
		criteria.addOrder(Order.asc("id"));
		if (updateUUID != null) {
			PullRequestUpdate update = find(updateUUID);
			if (update != null) {
				criteria.add(Restrictions.gt("id", update.getId()));
			}
		}
		return query(criteria);
	}

}
