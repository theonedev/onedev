package io.onedev.server.entitymanager.impl;

import io.onedev.server.entitymanager.ReviewedDiffManager;
import io.onedev.server.event.Listen;
import io.onedev.server.event.system.SystemStarting;
import io.onedev.server.event.system.SystemStopping;
import io.onedev.server.model.ReviewedDiff;
import io.onedev.server.model.User;
import io.onedev.server.persistence.annotation.Sessional;
import io.onedev.server.persistence.annotation.Transactional;
import io.onedev.server.persistence.dao.BaseEntityManager;
import io.onedev.server.persistence.dao.Dao;
import io.onedev.server.taskschedule.SchedulableTask;
import io.onedev.server.taskschedule.TaskScheduler;
import org.hibernate.criterion.Restrictions;
import org.joda.time.DateTime;
import org.quartz.CronScheduleBuilder;
import org.quartz.ScheduleBuilder;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.HashMap;
import java.util.Map;

import static io.onedev.server.model.ReviewedDiff.*;

@Singleton
public class DefaultReviewedDiffManager extends BaseEntityManager<ReviewedDiff> 
		implements ReviewedDiffManager, SchedulableTask {
	
	private static final int MAX_PRESERVE_DAYS = 365;
	
	private final TaskScheduler taskScheduler;
	
	private volatile String taskId;
	
	@Inject
	public DefaultReviewedDiffManager(Dao dao, TaskScheduler taskScheduler) {
		super(dao);
		this.taskScheduler = taskScheduler;
	}

	@Sessional
	@Override
	public Map<String, ReviewedDiff> query(User user, String oldCommitHash, String newCommitHash) {
		var statuses = new HashMap<String, ReviewedDiff>();
		var criteria = newCriteria();
		criteria.add(Restrictions.eq(PROP_USER, user));
		criteria.add(Restrictions.eq(PROP_OLD_COMMIT_HASH, oldCommitHash));
		criteria.add(Restrictions.eq(PROP_NEW_COMMIT_HASH, newCommitHash));
		for (var status: query(criteria)) 
			statuses.put(status.getBlobPath(), status);
		return statuses;
	}

	@Transactional
	@Override
	public void createOrUpdate(ReviewedDiff status) {
		dao.persist(status);
	}

	@Listen
	public void on(SystemStarting event) {
		taskId = taskScheduler.schedule(this);
	}

	@Listen
	public void on(SystemStopping event) {
		taskScheduler.unschedule(taskId);
	}

	@Transactional
	@Override
	public void execute() {
		var query = getSession().createQuery("delete from ReviewedDiff where date < :date");
		query.setParameter("date", new DateTime().minusDays(MAX_PRESERVE_DAYS).toDate());
		query.executeUpdate();
	}

	@Override
	public ScheduleBuilder<?> getScheduleBuilder() {
		return CronScheduleBuilder.dailyAtHourAndMinute(2, 30);
	}
	
}
