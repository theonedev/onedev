package io.onedev.server.build;

import java.io.Serializable;

import io.onedev.server.web.editable.annotation.Editable;

@Editable
public class DependencySpec implements Serializable {

	private static final long serialVersionUID = 1L;

	private String jobSpec;

	@Editable
	public String getJobSpec() {
		return jobSpec;
	}

	public void setJobSpec(String jobSpec) {
		this.jobSpec = jobSpec;
	}
	
}
