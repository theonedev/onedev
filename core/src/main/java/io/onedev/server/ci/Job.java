package io.onedev.server.ci;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.validator.constraints.NotEmpty;

import io.onedev.server.ci.jobtrigger.JobTrigger;
import io.onedev.server.util.inputspec.InputSpec;
import io.onedev.server.web.editable.annotation.Editable;
import io.onedev.server.web.editable.annotation.Horizontal;
import io.onedev.server.web.editable.annotation.PathPatterns;

@Editable
@Horizontal
public class Job implements Serializable {

	private static final long serialVersionUID = 1L;

	private String name;
	
	private List<Dependency> dependencies = new ArrayList<>();
	
	private List<InputSpec> params = new ArrayList<>();
	
	private List<Step> steps = new ArrayList<>();
	
	private List<JobTrigger> triggers = new ArrayList<>();
	
	private String publishArtifacts;
	
	private long timeout = 3600;
	
	@Editable(order=100, description="Specify name of the job")
	@NotEmpty
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Editable(order=200, name="Prompt Parameters", description="Specify parameters to prompt when the job "
			+ "is triggered manually")
	public List<InputSpec> getParams() {
		return params;
	}

	public void setParams(List<InputSpec> params) {
		this.params = params;
	}

	@Editable(order=300)
	public List<Step> getSteps() {
		return steps;
	}

	public void setSteps(List<Step> steps) {
		this.steps = steps;
	}
	
	@Editable(name="Dependency Jobs", order=400, description="Job dependencies determines the order and "
			+ "concurrency when run different jobs")
	public List<Dependency> getDependencies() {
		return dependencies;
	}

	public void setDependencies(List<Dependency> dependencies) {
		this.dependencies = dependencies;
	}

	@Editable(order=500, description="Use triggers to run the job automatically under certain conditions")
	public List<JobTrigger> getTriggers() {
		return triggers;
	}

	public void setTriggers(List<JobTrigger> triggers) {
		this.triggers = triggers;
	}

	@Editable(order=600, description="Optionally specify space-separated workspace files to publish as artifacts. "
			+ "Use * or ? for wildcard match")
	@PathPatterns
	public String getPublishArtifacts() {
		return publishArtifacts;
	}

	public void setPublishArtifacts(String publishArtifacts) {
		this.publishArtifacts = publishArtifacts;
	}

	@Editable(order=600, description="Specify timeout in seconds")
	public long getTimeout() {
		return timeout;
	}

	public void setTimeout(long timeout) {
		this.timeout = timeout;
	}

}
