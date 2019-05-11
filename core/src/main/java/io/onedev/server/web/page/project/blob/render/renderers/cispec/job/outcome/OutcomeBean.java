package io.onedev.server.web.page.project.blob.render.renderers.cispec.job.outcome;

import java.io.Serializable;

import javax.validation.constraints.NotNull;

import io.onedev.server.ci.job.JobOutcome;
import io.onedev.server.web.editable.annotation.Editable;

@Editable
public class OutcomeBean implements Serializable {

	private static final long serialVersionUID = 1L;

	private JobOutcome outcome;

	@Editable(name="Type", order=100)
	@NotNull(message="may not be empty")
	public JobOutcome getOutcome() {
		return outcome;
	}

	public void setOutcome(JobOutcome outcome) {
		this.outcome = outcome;
	}

}
