package io.onedev.server.service.impl;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Singleton;

import org.hibernate.criterion.Restrictions;

import io.onedev.server.model.Issue;
import io.onedev.server.model.IssueWatch;
import io.onedev.server.model.User;
import io.onedev.server.persistence.annotation.Transactional;
import io.onedev.server.persistence.dao.EntityCriteria;
import io.onedev.server.service.IssueWatchService;
import io.onedev.server.util.watch.WatchStatus;

@Singleton
public class DefaultIssueWatchService extends BaseEntityService<IssueWatch>
		implements IssueWatchService {

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
			createOrUpdate(watch);
		}
	}

	@Transactional
	@Override
	public void createOrUpdate(IssueWatch watch) {
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
				else if (watchStatus == WatchStatus.IGNORE) 
					watch.setWatching(false);
				else 
					delete(watch);
			} else if (watchStatus != WatchStatus.DEFAULT) {
				watch = new IssueWatch();
				watch.setIssue(issue);
				watch.setUser(user);
				watch.setWatching(watchStatus == WatchStatus.WATCH);
				createOrUpdate(watch);
			}
		}
    }

}
