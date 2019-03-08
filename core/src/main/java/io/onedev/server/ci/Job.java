package io.onedev.server.ci;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.validator.constraints.NotEmpty;

import io.onedev.server.ci.jobtrigger.JobTrigger;
import io.onedev.server.util.inputspec.InputSpec;
import io.onedev.server.web.editable.annotation.Editable;
import io.onedev.server.web.editable.annotation.Horizontal;

@Editable
@Horizontal
public class Job implements Serializable {

	private static final long serialVersionUID = 1L;

	private String name;
	
	private List<Dependency> dependencies = new ArrayList<>();
	
	private List<InputSpec> params = new ArrayList<>();
	
	private List<Step> steps = new ArrayList<>();
	
	private List<JobTrigger> triggers = new ArrayList<>();
	
	private String artifacts;
	
	private long timeout = 3600;
	
	@Editable(order=100)
	@NotEmpty
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Editable(order=200)
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
	
	@Editable(order=400)
	public List<Dependency> getDependencies() {
		return dependencies;
	}

	public void setDependencies(List<Dependency> dependencies) {
		this.dependencies = dependencies;
	}

	@Editable(order=500)
	public List<JobTrigger> getTriggers() {
		return triggers;
	}

	public void setTriggers(List<JobTrigger> triggers) {
		this.triggers = triggers;
	}

	@Editable(order=500)
	public String getArtifacts() {
		return artifacts;
	}

	public void setArtifacts(String artifacts) {
		this.artifacts = artifacts;
	}

	@Editable(order=600, description="Timeout in seconds")
	public long getTimeout() {
		return timeout;
	}

	public void setTimeout(long timeout) {
		this.timeout = timeout;
	}

}
