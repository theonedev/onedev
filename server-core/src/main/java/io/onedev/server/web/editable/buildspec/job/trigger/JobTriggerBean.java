package io.onedev.server.web.editable.buildspec.job.trigger;

import java.io.Serializable;

import javax.validation.constraints.NotNull;

import io.onedev.server.buildspec.job.trigger.JobTrigger;
import io.onedev.server.annotation.Editable;

@Editable
public class JobTriggerBean implements Serializable {

	private static final long serialVersionUID = 1L;

	private JobTrigger trigger;

	@Editable(name="Type", order=100)
	@NotNull(message="may not be empty")
	public JobTrigger getTrigger() {
		return trigger;
	}

	public void setTrigger(JobTrigger trigger) {
		this.trigger = trigger;
	}

}
