package io.onedev.server.web.page.project.blob.render.renderers.cispec.job.trigger;

import java.io.Serializable;

import javax.validation.constraints.NotNull;

import io.onedev.server.ci.job.trigger.JobTrigger;
import io.onedev.server.web.editable.annotation.Editable;

@Editable
public class TriggerBean implements Serializable {

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
