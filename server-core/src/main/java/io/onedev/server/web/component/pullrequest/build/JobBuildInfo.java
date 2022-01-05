package io.onedev.server.web.component.pullrequest.build;

import java.util.List;

import io.onedev.server.model.Build;

public class JobBuildInfo {
	
	private final String jobName;

	private final boolean required;
	
	private final List<Build> builds;
	
	public JobBuildInfo(String jobName, boolean required, List<Build> builds) {
		this.jobName = jobName;
		this.required = required;
		this.builds = builds;
	}

	public String getJobName() {
		return jobName;
	}

	public boolean isRequired() {
		return required;
	}

	public List<Build> getBuilds() {
		return builds;
	}
	
}