package io.onedev.server.service.impl;

import static io.onedev.server.model.Stopwatch.PROP_ISSUE;
import static io.onedev.server.model.Stopwatch.PROP_USER;

import java.util.Date;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.hibernate.criterion.Restrictions;

import io.onedev.commons.utils.ExplicitException;
import io.onedev.server.model.Issue;
import io.onedev.server.model.IssueWork;
import io.onedev.server.model.Stopwatch;
import io.onedev.server.model.User;
import io.onedev.server.persistence.annotation.Sessional;
import io.onedev.server.persistence.annotation.Transactional;
import io.onedev.server.service.IssueWorkService;
import io.onedev.server.service.StopwatchService;

@Singleton
public class DefaultStopwatchService extends BaseEntityService<Stopwatch> implements StopwatchService {

	@Inject
	private IssueWorkService workService;

	@Override
	@Sessional
	public Stopwatch find(User user, Issue issue) {
		var criteria = newCriteria();
		criteria.add(Restrictions.eq(PROP_USER, user));
		criteria.add(Restrictions.eq(PROP_ISSUE, issue));
		return find(criteria);
	}
	
	@Transactional
	@Override
	public Stopwatch startWork(User user, Issue issue) {
		var watch = find(user, issue);
		if (watch == null) {
			watch = new Stopwatch();
			watch.setUser(user);
			watch.setIssue(issue);
			watch.setDate(new Date());
			dao.persist(watch);
			return watch;
		} else {
			throw new ExplicitException("Work already started");
		}
	}

	@Transactional
	@Override
	public void stopWork(Stopwatch stopwatch) {
		int spentMinutes = (int) ((System.currentTimeMillis() - stopwatch.getDate().getTime()) / 60000);
		if (spentMinutes > 0) {
			var work = new IssueWork();
			work.setUser(stopwatch.getUser());
			work.setIssue(stopwatch.getIssue());
			work.setDate(stopwatch.getDate());
			work.setMinutes(spentMinutes);
			workService.createOrUpdate(work);
		}
		dao.remove(stopwatch);
	}
		
}