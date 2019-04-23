package io.onedev.server.model.support.issue.transitiontrigger;

import org.hibernate.validator.constraints.NotEmpty;

import io.onedev.server.web.editable.annotation.JobChoice;
import io.onedev.server.web.editable.annotation.Editable;

@Editable(order=400, name="Build fixing the issue is successful")
public class BuildSuccessfulTrigger implements TransitionTrigger {

	private static final long serialVersionUID = 1L;

	private String jobName;
	
	@Editable(order=100, description="Specify job of the build")
	@JobChoice
	@NotEmpty
	public String getJobName() {
		return jobName;
	}

	public void setJobName(String jobName) {
		this.jobName = jobName;
	}

}
