package io.onedev.server.entitymanager.impl;

import com.google.common.collect.Lists;
import io.onedev.commons.utils.ExplicitException;
import io.onedev.server.event.Listen;
import io.onedev.server.event.system.SystemStarted;
import io.onedev.server.event.system.SystemStopping;
import io.onedev.server.mail.MailManager;
import io.onedev.server.entitymanager.IssueWorkManager;
import io.onedev.server.entitymanager.SettingManager;
import io.onedev.server.entitymanager.StopwatchManager;
import io.onedev.server.model.Issue;
import io.onedev.server.model.IssueWork;
import io.onedev.server.model.Stopwatch;
import io.onedev.server.model.User;
import io.onedev.server.model.support.administration.emailtemplates.EmailTemplates;
import io.onedev.server.persistence.annotation.Sessional;
import io.onedev.server.persistence.annotation.Transactional;
import io.onedev.server.persistence.dao.BaseEntityManager;
import io.onedev.server.persistence.dao.Dao;
import io.onedev.server.taskschedule.SchedulableTask;
import io.onedev.server.taskschedule.TaskScheduler;
import io.onedev.server.util.DateUtils;
import org.hibernate.criterion.Restrictions;
import org.quartz.CronScheduleBuilder;
import org.quartz.ScheduleBuilder;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static io.onedev.server.model.Stopwatch.PROP_ISSUE;
import static io.onedev.server.model.Stopwatch.PROP_USER;

@Singleton
public class DefaultStopwatchManager extends BaseEntityManager<Stopwatch> implements StopwatchManager, SchedulableTask {
	
	private final IssueWorkManager workManager;

	private final TaskScheduler taskScheduler;
	
	private final MailManager mailManager;
	
	private final SettingManager settingManager;
	
	private String taskId;
	
	@Inject
	public DefaultStopwatchManager(Dao dao, IssueWorkManager workManager, SettingManager settingManager,
								   TaskScheduler taskScheduler, MailManager mailManager) {
		super(dao);
		this.workManager = workManager;
		this.taskScheduler = taskScheduler;
		this.mailManager = mailManager;
		this.settingManager = settingManager;
	}

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
		stopWork(stopwatch, new Date());
	}

	private void stopWork(Stopwatch stopwatch, Date stopDate) {
		int spentMinutes = (int) ((System.currentTimeMillis() - stopwatch.getDate().getTime()) / 60000);
		if (spentMinutes > 0) {
			var day = DateUtils.toLocalDate(stopwatch.getDate()).toEpochDay();
			var works = workManager.query(stopwatch.getUser(), stopwatch.getIssue(), day);
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
				workWithoutNote.setUser(stopwatch.getUser());
				workWithoutNote.setIssue(stopwatch.getIssue());
				workWithoutNote.setDate(new Date());
			}
			workWithoutNote.setMinutes(workWithoutNote.getMinutes() + spentMinutes);
			workManager.createOrUpdate(workWithoutNote);
		}
		dao.remove(stopwatch);
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
		for (var stopwatch: query(true)) {
			var startDay = DateUtils.toLocalDate(stopwatch.getDate()).toEpochDay();
			if (startDay < day) {
				stopWork(stopwatch, DateUtils.toDate(LocalDate.ofEpochDay(startDay).atTime(23, 59)));

				Map<String, Object> bindings = new HashMap<>();
				bindings.put("stopwatch", stopwatch);

				var template = settingManager.getEmailTemplates().getStopwatchOverdue();
				var htmlBody = EmailTemplates.evalTemplate(true, template, bindings);
				var textBody = EmailTemplates.evalTemplate(false, template, bindings);

				var emailAddress = stopwatch.getUser().getPrimaryEmailAddress();
				if (emailAddress != null && emailAddress.isVerified()) {
					mailManager.sendMail(Arrays.asList(emailAddress.getValue()),
							Lists.newArrayList(), Lists.newArrayList(),
							"[Stopwatch Overdue] Your Issue Stopwatch is Overdue",
							htmlBody, textBody, null, null, null);
				}
			}
		}
	}

	@Override
	public ScheduleBuilder<?> getScheduleBuilder() {
		return CronScheduleBuilder.cronSchedule("0 0 * * * ?");
	}
	
}