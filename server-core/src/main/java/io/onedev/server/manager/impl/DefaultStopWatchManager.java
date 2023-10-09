package io.onedev.server.manager.impl;

import io.onedev.commons.utils.ExplicitException;
import io.onedev.server.event.Listen;
import io.onedev.server.event.system.SystemStarted;
import io.onedev.server.event.system.SystemStopping;
import io.onedev.server.manager.IssueWorkManager;
import io.onedev.server.manager.StopWatchManager;
import io.onedev.server.model.Issue;
import io.onedev.server.model.IssueWork;
import io.onedev.server.model.StopWatch;
import io.onedev.server.model.User;
import io.onedev.server.persistence.annotation.Sessional;
import io.onedev.server.persistence.annotation.Transactional;
import io.onedev.server.persistence.dao.BaseEntityManager;
import io.onedev.server.persistence.dao.Dao;
import io.onedev.server.persistence.dao.EntityCriteria;
import io.onedev.server.util.DateUtils;
import io.onedev.server.taskschedule.SchedulableTask;
import io.onedev.server.taskschedule.TaskScheduler;
import org.hibernate.criterion.Restrictions;
import org.quartz.CronScheduleBuilder;
import org.quartz.ScheduleBuilder;
import org.quartz.SimpleScheduleBuilder;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.time.LocalDate;
import java.util.Date;

import static io.onedev.server.model.StopWatch.PROP_ISSUE;
import static io.onedev.server.model.StopWatch.PROP_USER;

@Singleton
public class DefaultStopWatchManager extends BaseEntityManager<StopWatch> implements StopWatchManager, SchedulableTask {
	
	private final IssueWorkManager workManager;

	private final TaskScheduler taskScheduler;
	
	private String taskId;
	
	@Inject
	public DefaultStopWatchManager(Dao dao, IssueWorkManager workManager, TaskScheduler taskScheduler) {
		super(dao);
		this.workManager = workManager;
		this.taskScheduler = taskScheduler;
	}

	@Override
	@Sessional
	public StopWatch find(User user, Issue issue) {
		var criteria = newCriteria();
		criteria.add(Restrictions.eq(PROP_USER, user));
		criteria.add(Restrictions.eq(PROP_ISSUE, issue));
		return find(criteria);
	}
	
	@Transactional
	@Override
	public StopWatch startWork(User user, Issue issue) {
		var watch = find(user, issue);
		if (watch == null) {
			watch = new StopWatch();
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
	public void stopWork(StopWatch stopWatch) {
		stopWork(stopWatch, new Date());
	}

	private void stopWork(StopWatch stopWatch, Date stopDate) {
		int spentMinutes = (int) ((System.currentTimeMillis() - stopWatch.getDate().getTime()) / 60000);
		if (spentMinutes > 0) {
			var day = DateUtils.toLocalDate(stopWatch.getDate()).toEpochDay();
			var works = workManager.query(stopWatch.getUser(), stopWatch.getIssue(), day);
			works.sort((o1, o2) -> o2.getDate().compareTo(o1.getDate()));
			IssueWork workWithoutNote = null;
			for (var work: works) {
				if (work.getNote() == null) {
					workWithoutNote = work;
					break;
				}
			}
			if (workWithoutNote == null) {
				workWithoutNote = new IssueWork();
				workWithoutNote.setDay(day);
				workWithoutNote.setUser(stopWatch.getUser());
				workWithoutNote.setIssue(stopWatch.getIssue());
				workWithoutNote.setDate(new Date());
			}
			workWithoutNote.setMinutes(workWithoutNote.getMinutes() + spentMinutes);
			workManager.createOrUpdate(workWithoutNote);
		}
		dao.remove(stopWatch);
	}
	
	@Listen
	public void on(SystemStarted event) {
		taskId = taskScheduler.schedule(this);
	}
	
	@Listen
	public void on(SystemStopping event) {
		if (taskId != null)
			taskScheduler.unschedule(taskId);
	}

	@Transactional
	@Override
	public void execute() {
		var day = DateUtils.toLocalDate(new Date()).toEpochDay();
		for (var stopWatch: query(true)) {
			var startDay = DateUtils.toLocalDate(stopWatch.getDate()).toEpochDay();
			if (startDay < day) 
				stopWork(stopWatch, DateUtils.toDate(LocalDate.ofEpochDay(startDay).atTime(23, 59)));
		}
	}

	@Override
	public ScheduleBuilder<?> getScheduleBuilder() {
		return CronScheduleBuilder.cronSchedule("0 0 * * * ?");
	}
	
}