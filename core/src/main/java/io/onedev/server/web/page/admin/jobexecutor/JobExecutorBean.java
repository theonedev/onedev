package io.onedev.server.web.page.admin.jobexecutor;

import java.io.Serializable;

import io.onedev.server.model.support.jobexecutor.JobExecutor;
import io.onedev.server.web.editable.annotation.Editable;
import io.onedev.server.web.editable.annotation.NameOfEmptyValue;

@Editable
public class JobExecutorBean implements Serializable {

	private static final long serialVersionUID = 1L;

	private JobExecutor jobExecutor;

	@Editable
	@NameOfEmptyValue("No job executor")
	public JobExecutor getJobExecutor() {
		return jobExecutor;
	}

	public void setJobExecutor(JobExecutor jobExecutor) {
		this.jobExecutor = jobExecutor;
	}

}
