package io.onedev.server.ci;

import java.io.Serializable;

import io.onedev.server.web.editable.annotation.Editable;

@Editable
public class DependencySpec implements Serializable {

	private static final long serialVersionUID = 1L;

	private String jobSpec;
	
	private String artifacts;

	@Editable(name="Job", order=100)
	public String getJobSpec() {
		return jobSpec;
	}

	public void setJobSpec(String jobSpec) {
		this.jobSpec = jobSpec;
	}

	@Editable(order=200)
	public String getArtifacts() {
		return artifacts;
	}

	public void setArtifacts(String artifacts) {
		this.artifacts = artifacts;
	}
	
}
