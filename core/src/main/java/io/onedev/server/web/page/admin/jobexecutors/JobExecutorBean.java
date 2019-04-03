package io.onedev.server.web.page.admin.jobexecutors;

import java.io.Serializable;

import javax.validation.constraints.NotNull;

import io.onedev.server.model.support.jobexecutor.JobExecutor;
import io.onedev.server.web.editable.annotation.Editable;

@Editable
public class JobExecutorBean implements Serializable {

	private static final long serialVersionUID = 1L;

	private JobExecutor jobExecutor;

	@Editable
	@NotNull
	public JobExecutor getJobExecutor() {
		return jobExecutor;
	}

	public void setJobExecutor(JobExecutor jobExecutor) {
		this.jobExecutor = jobExecutor;
	}
	
}
