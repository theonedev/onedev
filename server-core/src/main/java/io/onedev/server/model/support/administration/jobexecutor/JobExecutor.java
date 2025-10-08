package io.onedev.server.model.support.administration.jobexecutor;

import com.google.common.base.Throwables;
import io.onedev.commons.loader.ExtensionPoint;
import io.onedev.commons.utils.TaskLogger;
import io.onedev.server.OneDev;
import io.onedev.server.annotation.DnsName;
import io.onedev.server.annotation.Editable;
import io.onedev.server.service.AgentService;
import io.onedev.server.service.BuildService;
import io.onedev.server.event.ListenerRegistry;
import io.onedev.server.event.project.build.BuildRunning;
import io.onedev.server.exception.ExceptionUtils;
import io.onedev.server.job.JobContext;
import io.onedev.server.job.match.JobMatch;
import io.onedev.server.model.Build;
import io.onedev.server.persistence.TransactionService;
import io.onedev.server.util.usage.Usage;
import io.onedev.server.web.util.WicketUtils;
import org.eclipse.jetty.http.HttpStatus;

import org.jspecify.annotations.Nullable;
import javax.validation.constraints.NotEmpty;
import java.io.Serializable;
import java.util.Date;

@ExtensionPoint
@Editable
public abstract class JobExecutor implements Serializable {

	private static final long serialVersionUID = 1L;

	private boolean enabled = true;
	
	private String name;
	
	private String jobMatch;
	
	private boolean htmlReportPublishEnabled;
	
	private boolean sitePublishEnabled;
	
	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	@Editable(order=10)
	@DnsName //this name may be used as namespace/network prefixes, so put a strict constraint
	@NotEmpty
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Editable(order=30, name="Enable Site Publish", group = "Privilege Settings", description="Enable this to allow to run site publish step. OneDev will serve project "
			+ "site files as is. To avoid XSS attack, make sure this executor can only be used by trusted jobs")
	public boolean isSitePublishEnabled() {
		return sitePublishEnabled;
	}

	public void setSitePublishEnabled(boolean sitePublishEnabled) {
		this.sitePublishEnabled = sitePublishEnabled;
	}

	@Editable(order=40, name="Enable Html Report Publish", group = "Privilege Settings", description = "Enable this to allow to run html report publish step. To avoid XSS attack, " +
			"make sure this executor can only be used by trusted jobs")
	public boolean isHtmlReportPublishEnabled() {
		return htmlReportPublishEnabled;
	}

	public void setHtmlReportPublishEnabled(boolean htmlReportPublishEnabled) {
		this.htmlReportPublishEnabled = htmlReportPublishEnabled;
	}

	@SuppressWarnings("unused")
	private static boolean isSubscriptionActive() {
		return WicketUtils.isSubscriptionActive();
	}

	@Editable(order=10000, name="Applicable Jobs", placeholder="Any job", description="Optionally specify applicable jobs of this executor")
	@io.onedev.server.annotation.JobMatch(withProjectCriteria = true, withJobCriteria = true)
	@Nullable
	public String getJobMatch() {
		return jobMatch;
	}

	public void setJobMatch(String jobMatch) {
		this.jobMatch = jobMatch;
	}
	
	public abstract boolean execute(JobContext jobContext, TaskLogger jobLogger);

	public Usage onDeleteProject(String projectPath) {
		Usage usage = new Usage();
		if (jobMatch != null && JobMatch.parse(jobMatch, true, true).isUsingProject(projectPath)) {
			usage.add("applicable jobs" );
		}
		return usage;
	}
	
	public void onMoveProject(String oldPath, String newPath) {
		if (jobMatch != null) {
			JobMatch parsedJobMatch = JobMatch.parse(jobMatch, true, true);
			parsedJobMatch.onMoveProject(oldPath, newPath);
			jobMatch = parsedJobMatch.toString();
		}
	}
	
	protected void notifyJobRunning(Long buildId, @Nullable Long agentId) {
		OneDev.getInstance(TransactionService.class).run(() -> {
			BuildService buildService = OneDev.getInstance(BuildService.class);
			Build build = buildService.load(buildId);
			build.setStatus(Build.Status.RUNNING);
			build.setRunningDate(new Date());
			if (agentId != null)
				build.setAgent(OneDev.getInstance(AgentService.class).load(agentId));
			buildService.update(build);
			OneDev.getInstance(ListenerRegistry.class).post(new BuildRunning(build));
		});
	}
	
	protected String getErrorMessage(Exception exception) {
		var response = ExceptionUtils.buildResponse(exception);
		if (response != null) 
			return response.getBody() != null? response.getBody().getText() : HttpStatus.getMessage(response.getStatus());
		else
			return Throwables.getStackTraceAsString(exception);
	}

}
