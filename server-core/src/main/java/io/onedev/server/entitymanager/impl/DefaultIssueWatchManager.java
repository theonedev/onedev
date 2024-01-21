package io.onedev.server.entitymanager.impl;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.google.common.base.Preconditions;
import io.onedev.server.persistence.annotation.Transactional;
import io.onedev.server.util.watch.WatchStatus;
import org.hibernate.criterion.Restrictions;

import io.onedev.server.entitymanager.IssueWatchManager;
import io.onedev.server.model.Issue;
import io.onedev.server.model.IssueWatch;
import io.onedev.server.model.User;
import io.onedev.server.persistence.dao.BaseEntityManager;
import io.onedev.server.persistence.dao.Dao;
import io.onedev.server.persistence.dao.EntityCriteria;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Singleton
public class DefaultIssueWatchManager extends BaseEntityManager<IssueWatch> 
		implements IssueWatchManager {

	@Inject
	public DefaultIssueWatchManager(Dao dao) {
		super(dao);
	}

	@Override
	public IssueWatch find(Issue issue, User user) {
		EntityCriteria<IssueWatch> criteria = newCriteria();
		criteria.add(Restrictions.eq("issue", issue));
		criteria.add(Restrictions.eq("user", user));
		return find(criteria);
	}

	@Override
	public void watch(Issue issue, User user, boolean watching) {
		IssueWatch watch = (IssueWatch) issue.getWatch(user, true);
		if (watch.isNew()) {
			watch.setWatching(watching);
			create(watch);
		}
	}

	@Transactional
	@Override
	public void create(IssueWatch watch) {
		Preconditions.checkState(watch.isNew());
		dao.persist(watch);
	}

	@Transactional
	@Override
	public void update(IssueWatch watch) {
		Preconditions.checkState(!watch.isNew());
		dao.persist(watch);
	}

	@Transactional
    @Override
    public void setWatchStatus(User user, Collection<Issue> issues, WatchStatus watchStatus) {
		Map<Long, IssueWatch> watchMap = new HashMap<>();
		for (var watch: user.getIssueWatches()) 
			watchMap.put(watch.getIssue().getId(), watch);
		
        for (var issue: issues) {
			var watch = watchMap.get(issue.getId());
			if (watch != null) {
				if (watchStatus == WatchStatus.WATCH) 
					watch.setWatching(true);
				else if (watchStatus == WatchStatus.DO_NOT_WATCH) 
					watch.setWatching(false);
				else 
					delete(watch);
			} else if (watchStatus != WatchStatus.DEFAULT) {
				watch = new IssueWatch();
				watch.setIssue(issue);
				watch.setUser(user);
				watch.setWatching(watchStatus == WatchStatus.WATCH);
				create(watch);
			}
		}
    }

}
