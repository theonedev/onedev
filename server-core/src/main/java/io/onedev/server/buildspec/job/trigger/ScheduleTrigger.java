package io.onedev.server.buildspec.job.trigger;

import io.onedev.server.buildspec.job.Job;
import io.onedev.server.buildspec.job.TriggerMatch;
import io.onedev.server.event.project.ProjectEvent;
import io.onedev.server.event.project.ScheduledTimeReaches;
import io.onedev.server.git.GitUtils;
import io.onedev.server.annotation.CronExpression;
import io.onedev.server.annotation.Editable;

import javax.validation.constraints.NotEmpty;

@Editable(order=600, name="Cron schedule")
public class ScheduleTrigger extends JobTrigger {

	private static final long serialVersionUID = 1L;

	private String cronExpression;
	
	@Editable(order=100, description="Specify a <a target='_blank' href='http://www.quartz-scheduler.org/documentation/quartz-2.3.0/tutorials/crontrigger.html#format'>cron schedule</a> to "
			+ "fire the job automatically. <b>Note:</b> this is only applicable to default branch")
	@CronExpression
	@NotEmpty
	public String getCronExpression() {
		return cronExpression;
	}

	public void setCronExpression(String cronExpression) {
		this.cronExpression = cronExpression;
	}

	@Override
	protected TriggerMatch triggerMatches(ProjectEvent event, Job job) {
		if (event instanceof ScheduledTimeReaches) {
			return new TriggerMatch(GitUtils.branch2ref(event.getProject().getDefaultBranch()), 
					null, null, getParams(), "Scheduled");
		} else {
			return null;
		}
	}

	@Override
	public String getTriggerDescription() {
		return "Schedule at " + cronExpression;
	}

}
