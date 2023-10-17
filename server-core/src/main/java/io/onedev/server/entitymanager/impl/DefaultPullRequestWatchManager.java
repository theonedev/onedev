package io.onedev.server.entitymanager.impl;

import com.google.common.base.Preconditions;
import io.onedev.server.entitymanager.PullRequestWatchManager;
import io.onedev.server.model.PullRequest;
import io.onedev.server.model.PullRequestWatch;
import io.onedev.server.model.User;
import io.onedev.server.persistence.annotation.Transactional;
import io.onedev.server.persistence.dao.BaseEntityManager;
import io.onedev.server.persistence.dao.Dao;
import io.onedev.server.persistence.dao.EntityCriteria;
import io.onedev.server.util.watch.WatchStatus;
import org.hibernate.criterion.Restrictions;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Singleton
public class DefaultPullRequestWatchManager extends BaseEntityManager<PullRequestWatch> 
		implements PullRequestWatchManager {

	@Inject
	public DefaultPullRequestWatchManager(Dao dao) {
		super(dao);
	}

	@Override
	public PullRequestWatch find(PullRequest request, User user) {
		EntityCriteria<PullRequestWatch> criteria = newCriteria();
		criteria.add(Restrictions.eq("request", request));
		criteria.add(Restrictions.eq("user", user));
		return find(criteria);
	}

	@Override
	public void watch(PullRequest request, User user, boolean watching) {
		PullRequestWatch watch = (PullRequestWatch) request.getWatch(user, true);
		if (watch.isNew()) {
			watch.setWatching(watching);
			dao.persist(watch);
		}
	}

	@Override
	public void create(PullRequestWatch watch) {
		Preconditions.checkState(watch.isNew());
		dao.persist(watch);
	}

	@Override
	public void update(PullRequestWatch watch) {
		Preconditions.checkState(!watch.isNew());
		dao.persist(watch);
	}

	@Transactional
	@Override
	public void setWatchStatus(User user, Collection<PullRequest> requests, WatchStatus watchStatus) {
		Map<Long, PullRequestWatch> watchMap = new HashMap<>();
		for (var watch: user.getPullRequestWatches())
			watchMap.put(watch.getRequest().getId(), watch);

		for (var request: requests) {
			var watch = watchMap.get(request.getId());
			if (watch != null) {
				if (watchStatus == WatchStatus.WATCH)
					watch.setWatching(true);
				else if (watchStatus == WatchStatus.DO_NOT_WATCH)
					watch.setWatching(false);
				else
					delete(watch);
			} else if (watchStatus != WatchStatus.DEFAULT) {
				watch = new PullRequestWatch();
				watch.setRequest(request);
				watch.setUser(user);
				watch.setWatching(watchStatus == WatchStatus.WATCH);
				create(watch);
			}
		}
	}
	
}
