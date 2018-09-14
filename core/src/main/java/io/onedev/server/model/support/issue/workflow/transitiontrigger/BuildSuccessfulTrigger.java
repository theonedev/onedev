package io.onedev.server.model.support.issue.workflow.transitiontrigger;

import org.hibernate.validator.constraints.NotEmpty;

import io.onedev.server.web.editable.annotation.ConfigurationChoice;
import io.onedev.server.web.editable.annotation.Editable;

@Editable(order=400, name="Build is successful")
public class BuildSuccessfulTrigger implements TransitionTrigger {

	private static final long serialVersionUID = 1L;

	private String configuration;
	
	@Editable(order=100, description="Specify the build configuration")
	@ConfigurationChoice
	@NotEmpty
	public String getConfiguration() {
		return configuration;
	}

	public void setConfiguration(String configuration) {
		this.configuration = configuration;
	}

}
