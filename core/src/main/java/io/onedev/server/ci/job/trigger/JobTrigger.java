package io.onedev.server.ci.job.trigger;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import io.onedev.server.ci.job.Job;
import io.onedev.server.ci.job.param.JobParam;
import io.onedev.server.event.ProjectEvent;
import io.onedev.server.web.editable.annotation.Editable;

@Editable
public abstract class JobTrigger implements Serializable {

	private static final long serialVersionUID = 1L;

	private List<JobParam> params = new ArrayList<>();

	@Editable(name="Trigger Parameters", order=1000, description="Specify parameters to trigger the job")
	public List<JobParam> getParams() {
		return params;
	}

	public void setParams(List<JobParam> params) {
		this.params = params;
	}
	
	public abstract boolean matches(ProjectEvent event, Job job);
	
	public abstract String getDescription();
	
}
