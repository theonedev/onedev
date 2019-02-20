package io.onedev.server.build;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import io.onedev.server.web.editable.annotation.Editable;

@Editable
public class BuildSpec implements Serializable {

	private static final long serialVersionUID = 1L;

	private List<JobSpec> jobs = new ArrayList<>();

	@Editable
	public List<JobSpec> getJobs() {
		return jobs;
	}

	public void setJobs(List<JobSpec> jobs) {
		this.jobs = jobs;
	}
	
}
