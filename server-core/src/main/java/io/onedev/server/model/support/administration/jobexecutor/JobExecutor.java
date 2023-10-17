package io.onedev.server.model.support.administration.jobexecutor;

import com.google.common.base.Throwables;
import io.onedev.commons.loader.ExtensionPoint;
import io.onedev.commons.utils.TaskLogger;
import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.AgentManager;
import io.onedev.server.entitymanager.BuildManager;
import io.onedev.server.event.ListenerRegistry;
import io.onedev.server.event.project.build.BuildRunning;
import io.onedev.server.job.JobContext;
import io.onedev.server.job.match.JobMatch;
import io.onedev.server.model.Build;
import io.onedev.server.persistence.TransactionManager;
import io.onedev.server.util.ExceptionUtils;
import io.onedev.server.util.usage.Usage;
import io.onedev.server.annotation.DnsName;
import io.onedev.server.annotation.Editable;
import io.onedev.server.annotation.ShowCondition;
import io.onedev.server.web.util.WicketUtils;

import javax.annotation.Nullable;
import javax.validation.constraints.NotEmpty;
import javax.ws.rs.core.Response;
import java.io.Serializable;
import java.util.Date;

@ExtensionPoint
@Editable
public abstract class JobExecutor implements Serializable {

	private static final long serialVersionUID = 1L;

	private boolean enabled = true;
	
	private String name;
	
	private String jobRequirement;
	
	private boolean shellAccessEnabled;
	
	private boolean htmlReportPublishEnabled;
	
	private boolean sitePublishEnabled;
	
	private int cacheTTL = 7;
	
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

	@Editable(order=20, description="Enable this to allow to open interactive shell to diagnose running builds. "
			+ "<b class='text-danger'>WARNING</b>: Users with shell access can take control of the node used by "
			+ "the executor. You should configure job requirement below to make sure the executor can only be "
			+ "used by trusted jobs if this option is enabled")
	@ShowCondition("isSubscriptionActive")
	public boolean isShellAccessEnabled() {
		return shellAccessEnabled;
	}

	public void setShellAccessEnabled(boolean shellAccessEnabled) {
		this.shellAccessEnabled = shellAccessEnabled;
	}

	@Editable(order=30, description="Enable this to allow to run site publish step. OneDev will serve project "
			+ "site files as is. To avoid XSS attack, make sure this executor can only be used by trusted jobs")
	public boolean isSitePublishEnabled() {
		return sitePublishEnabled;
	}

	public void setSitePublishEnabled(boolean sitePublishEnabled) {
		this.sitePublishEnabled = sitePublishEnabled;
	}

	@Editable(order=40, description = "Enable this to allow to run html report publish step. To avoid XSS attach, " +
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

	@Editable(order=10000, placeholder="Any job", description="Optionally specify job requirement of this executor")
	@io.onedev.server.annotation.JobMatch(withProjectCriteria = true, withJobCriteria = true)
	@Nullable
	public String getJobRequirement() {
		return jobRequirement;
	}

	public void setJobRequirement(String jobRequirement) {
		this.jobRequirement = jobRequirement;
	}

	@Editable(order=50000, group="More Settings", description="Specify job cache TTL (time to live) by days. "
			+ "OneDev may create multiple job caches even for same cache key to avoid cache conflicts when "
			+ "running jobs concurrently. This setting tells OneDev to remove caches inactive for specified "
			+ "time period to save disk space")
	public int getCacheTTL() {
		return cacheTTL;
	}

	public void setCacheTTL(int cacheTTL) {
		this.cacheTTL = cacheTTL;
	}
	
	public abstract void execute(JobContext jobContext, TaskLogger jobLogger);
	
	public boolean isPlaceholderAllowed() {
		return true;
	}
	
	public Usage onDeleteProject(String projectPath) {
		Usage usage = new Usage();
		if (jobRequirement != null && JobMatch.parse(jobRequirement, true, true).isUsingProject(projectPath)) {
			usage.add("job requirement" );
		}
		return usage;
	}
	
	public void onMoveProject(String oldPath, String newPath) {
		if (jobRequirement != null) {
			JobMatch jobMatch = JobMatch.parse(jobRequirement, true, true);
			jobMatch.onMoveProject(oldPath, newPath);
			jobRequirement = jobMatch.toString();
		}
	}

	public Usage onDeleteUser(String userName) {
		Usage usage = new Usage();
		if (jobRequirement != null && JobMatch.parse(jobRequirement, true, true).isUsingUser(userName)) {
			usage.add("job requirement");
		}
		return usage;
	}
	
	public void onRenameUser(String oldName, String newName) {
		if (jobRequirement != null) {
			JobMatch jobMatch = JobMatch.parse(jobRequirement, true, true);
			jobMatch.onRenameUser(oldName, newName);
			jobRequirement = jobMatch.toString();
		}
	}

	public Usage onDeleteGroup(String groupName) {
		Usage usage = new Usage();
		if (jobRequirement != null && JobMatch.parse(jobRequirement, true, true).isUsingGroup(groupName)) {
			usage.add("job requirement");
		}
		return usage;
	}

	public void onRenameGroup(String oldName, String newName) {
		if (jobRequirement != null) {
			JobMatch jobMatch = JobMatch.parse(jobRequirement, true, true);
			jobMatch.onRenameGroup(oldName, newName);
			jobRequirement = jobMatch.toString();
		}
	}

	public Usage onDeleteRole(String roleName) {
		Usage usage = new Usage();
		if (jobRequirement != null && JobMatch.parse(jobRequirement, true, true).isUsingRole(roleName)) {
			usage.add("job requirement");
		}
		return usage;
	}

	public void onRenameRole(String oldName, String newName) {
		if (jobRequirement != null) {
			JobMatch jobMatch = JobMatch.parse(jobRequirement, true, true);
			jobMatch.onRenameRole(oldName, newName);
			jobRequirement = jobMatch.toString();
		}
	}
	
	protected void notifyJobRunning(Long buildId, @Nullable Long agentId) {
		OneDev.getInstance(TransactionManager.class).run(() -> {
			BuildManager buildManager = OneDev.getInstance(BuildManager.class);
			Build build = buildManager.load(buildId);
			build.setStatus(Build.Status.RUNNING);
			build.setRunningDate(new Date());
			if (agentId != null)
				build.setAgent(OneDev.getInstance(AgentManager.class).load(agentId));
			buildManager.update(build);
			OneDev.getInstance(ListenerRegistry.class).post(new BuildRunning(build));
		});
	}
	
	protected String getErrorMessage(Exception exception) {
		Response response = ExceptionUtils.buildResponse(exception);
		if (response != null) 
			return response.getEntity().toString();
		else
			return Throwables.getStackTraceAsString(exception);
	}

}
