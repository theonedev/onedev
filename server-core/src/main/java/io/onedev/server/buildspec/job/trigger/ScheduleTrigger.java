package io.onedev.server.buildspec.job.trigger;

import io.onedev.commons.codeassist.InputSuggestion;
import io.onedev.server.annotation.CronExpression;
import io.onedev.server.annotation.Editable;
import io.onedev.server.annotation.Patterns;
import io.onedev.server.buildspec.job.Job;
import io.onedev.server.buildspec.job.TriggerMatch;
import io.onedev.server.event.project.ProjectEvent;
import io.onedev.server.event.project.ScheduledTimeReaches;
import io.onedev.server.git.GitUtils;
import io.onedev.server.model.Project;
import io.onedev.commons.utils.match.Matcher;
import io.onedev.commons.utils.match.PathMatcher;
import io.onedev.server.util.patternset.PatternSet;
import io.onedev.server.web.util.SuggestionUtils;

import javax.validation.constraints.NotEmpty;
import java.util.List;

@Editable(order=600, name="Cron schedule")
public class ScheduleTrigger extends JobTrigger {

	private static final long serialVersionUID = 1L;

	private String cronExpression;
	
	private String branches;
	
	@Editable(order=100, description="Specify a <a target='_blank' href='http://www.quartz-scheduler.org/documentation/quartz-2.3.0/tutorials/crontrigger.html#format'>cron schedule</a> to "
			+ "fire the job automatically. <b class='text-info'>Note:</b> To save resource, seconds in cron expression will be ignored, and the minimum schedule interval is one minute")
	@CronExpression
	@NotEmpty
	public String getCronExpression() {
		return cronExpression;
	}

	public void setCronExpression(String cronExpression) {
		this.cronExpression = cronExpression;
	}

	@Editable(name="Applicable Branches", order=200, placeholder="Default branch", description=""
			+ "Optionally specify space-separated branches applicable for this trigger. "
			+ "Use '**', '*' or '?' for <a href='https://docs.onedev.io/appendix/path-wildcard' target='_blank'>path wildcard match</a>. "
			+ "Prefix with '-' to exclude. Leave empty for default branch")
	@Patterns(suggester="suggestBranches", path=true)
	public String getBranches() {
		return branches;
	}

	public void setBranches(String branches) {
		this.branches = branches;
	}

	@SuppressWarnings("unused")
	private static List<InputSuggestion> suggestBranches(String matchWith) {
		return SuggestionUtils.suggestBranches(Project.get(), matchWith);
	}
	
	@Override
	protected TriggerMatch triggerMatches(ProjectEvent event, Job job) {
		if (event instanceof ScheduledTimeReaches) {
			String branch = ((ScheduledTimeReaches) event).getBranch();
			Matcher matcher = new PathMatcher();

			if (branches == null && branch.equals(event.getProject().getDefaultBranch())
					|| PatternSet.parse(branches).matches(matcher, branch)) {
				return new TriggerMatch(GitUtils.branch2ref(branch),
						null, null, getParamMatrix(), getExcludeParamMaps(), "Scheduled");
			}
		}
		return null;
	}

	@Override
	public String getTriggerDescription() {
		var builder = new StringBuilder("Schedule at ").append(cronExpression);
		if (getBranches() != null)
			builder.append(" for branches '").append(getBranches()).append("'");
		else 
			builder.append(" for default branch");
		return builder.toString();
	}

}
