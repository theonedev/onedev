package io.onedev.server.job;

import io.onedev.k8shelper.Action;
import io.onedev.k8shelper.LeafFacade;
import io.onedev.server.buildspec.Service;
import io.onedev.server.buildspec.job.CacheSpec;
import io.onedev.server.model.support.administration.jobexecutor.JobExecutor;
import org.eclipse.jgit.lib.ObjectId;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

public class JobContext implements Serializable {
	
	private static final long serialVersionUID = 1L;

	private final String jobToken;
	
	private final JobExecutor jobExecutor;

	private final Long projectId;
	
	private final String projectPath;
	
	private final String projectGitDir;
	
	private final Long buildId;
	
	private final Long buildNumber;
	
	private final List<Action> actions;
	
	private final String refName;
	
	private final ObjectId commitId;
	
	private final Collection<CacheSpec> cacheSpecs; 
	
	private final List<Service> services;
	
	private final long timeout;
	
	private final int retried;
	
	public JobContext(String jobToken, JobExecutor jobExecutor, Long projectId, String projectPath, 
			String projectGitDir, Long buildId, Long buildNumber, List<Action> actions, 
			String refName, ObjectId commitId, Collection<CacheSpec> caches, 
			List<Service> services, long timeout, int retried) {
		this.jobToken = jobToken;
		this.jobExecutor = jobExecutor;
		this.projectId = projectId;
		this.projectPath = projectPath;
		this.projectGitDir = projectGitDir;
		this.buildId = buildId;
		this.buildNumber = buildNumber;
		this.actions = actions;
		this.refName = refName;
		this.commitId = commitId;
		this.cacheSpecs = caches;
		this.services = services;
		this.timeout = timeout;
		this.retried = retried;
	}
	
	public String getJobToken() {
		return jobToken;
	}

	public JobExecutor getJobExecutor() {
		return jobExecutor;
	}

	public Long getBuildId() {
		return buildId;
	}

	public List<Action> getActions() {
		return actions;
	}

	public String getRefName() {
		return refName;
	}

	public ObjectId getCommitId() {
		return commitId;
	}

	public Collection<CacheSpec> getCacheSpecs() {
		return cacheSpecs;
	}

	public List<Service> getServices() {
		return services;
	}

	public Long getProjectId() {
		return projectId;
	}

	public String getProjectPath() {
		return projectPath;
	}

	public String getProjectGitDir() {
		return projectGitDir;
	}

	public Long getBuildNumber() {
		return buildNumber;
	}

	public long getTimeout() {
		return timeout;
	}

	public int getRetried() {
		return retried;
	}

	public LeafFacade getStep(List<Integer> stepPosition) {
		return LeafFacade.of(actions, stepPosition);
	}
	
}
