package io.onedev.server.ci;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import io.onedev.server.web.editable.annotation.Editable;

@Editable
public class CISpec implements Serializable {

	private static final long serialVersionUID = 1L;
	
	public static final String BLOB_PATH = "onedev-ci.xml";

	private List<JobSpec> jobs = new ArrayList<>();

	@Editable
	public List<JobSpec> getJobs() {
		return jobs;
	}

	public void setJobs(List<JobSpec> jobs) {
		this.jobs = jobs;
	}
	
}
