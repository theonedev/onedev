package io.onedev.server.model.support.jobexecutor;

import java.io.Serializable;
import java.util.List;

import javax.annotation.Nullable;

import io.onedev.server.web.editable.annotation.Editable;
import io.onedev.server.web.editable.annotation.Patterns;
import io.onedev.server.web.editable.annotation.ProjectPatterns;

@Editable
public abstract class JobExecutor implements Serializable {

	private static final long serialVersionUID = 1L;

	private boolean enabled = true;
	
	private String projects;
	
	private String branches;
	
	private String jobs;
	
	private String environments;

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}
	
	@Editable(order=100, name="Applicable Projects", 
			description="Optionally specify space-separated projects applicable for this executor. Use * or ? for wildcard match. "
					+ "Leave empty to match all projects")
	@ProjectPatterns
	public String getProjects() {
		return projects;
	}

	public void setProjects(String projects) {
		this.projects = projects;
	}

	@Editable(order=200, name="Applicable Branches", 
			description="Optionally specify space-separated branches applicable for this executor. When run a job "
					+ "against a particular commit, all branches able to reaching the commit will be checked. "
					+ "Use * or ? for wildcard match. Leave empty to match all branches.")
	@Patterns
	public String getBranches() {
		return branches;
	}

	public void setBranches(String branches) {
		this.branches = branches;
	}

	@Editable(order=300, name="Applicable Jobs", 
			description="Optionally specify space-separated jobs applicable for this executor. Use * or ? for wildcard match. "
					+ "Leave empty to match all jobs")
	@Patterns
	public String getJobs() {
		return jobs;
	}

	public void setJobs(String jobs) {
		this.jobs = jobs;
	}

	@Editable(order=400, name="Applicable Environments", 
			description="Optionally specify space-separated environments applicable for this executor. Use * or ? for wildcard match. "
					+ "Leave empty to match all images")
	@Patterns
	public String getEnvironments() {
		return environments;
	}

	public void setEnvironments(String environments) {
		this.environments = environments;
	}
	
	@Nullable
	public abstract String run(String environment, List<String> commands);

	public abstract boolean isRunning(String runningInstance);
	
	public abstract void stop(String runningInstance);
	
}
