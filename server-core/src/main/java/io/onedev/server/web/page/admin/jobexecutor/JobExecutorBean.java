package io.onedev.server.web.page.admin.jobexecutor;

import java.io.Serializable;

import javax.validation.constraints.NotNull;

import io.onedev.server.model.support.administration.jobexecutor.JobExecutor;
import io.onedev.server.web.editable.annotation.Editable;

@Editable
public class JobExecutorBean implements Serializable {

	private static final long serialVersionUID = 1L;

	private JobExecutor executor;

	@Editable(name="Type")
	@NotNull
	public JobExecutor getExecutor() {
		return executor;
	}

	public void setExecutor(JobExecutor executor) {
		this.executor = executor;
	}
	
}
