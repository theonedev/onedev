package io.onedev.server.model.support.issue.workflow.transitiontrigger;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.validator.constraints.NotEmpty;

import io.onedev.server.util.inputspec.InputSpec;
import io.onedev.server.web.editable.annotation.ChoiceProvider;
import io.onedev.server.web.editable.annotation.ConfigurationChoice;
import io.onedev.server.web.editable.annotation.Editable;
import io.onedev.server.web.page.project.ProjectPage;
import io.onedev.server.web.util.WicketUtils;

@Editable(order=400, name="Build is successful")
public class BuildSuccessfulTrigger implements TransitionTrigger {

	private static final long serialVersionUID = 1L;

	private String configuration;
	
	private String buildField;

	@Editable(order=100, description="Specify the build configuration")
	@ConfigurationChoice
	@NotEmpty
	public String getConfiguration() {
		return configuration;
	}

	public void setConfiguration(String configuration) {
		this.configuration = configuration;
	}

	@Editable(order=200, description="Optionally specify an issue field of \"build choice\" type to store the build information")
	@ChoiceProvider("getBuildFieldChoices")
	public String getBuildField() {
		return buildField;
	}

	public void setBuildField(String buildField) {
		this.buildField = buildField;
	}

	@Override
	public Button getButton() {
		return null;
	}
	
	@SuppressWarnings("unused")
	private static List<String> getBuildFieldChoices() {
		List<String> choices = new ArrayList<>();
		ProjectPage page = (ProjectPage) WicketUtils.getPage();
		for (InputSpec field: page.getProject().getIssueWorkflow().getFieldSpecs()) {
			if (field.getType().equals(InputSpec.BUILD))
				choices.add(field.getName());
		}
		return choices;
	}
	
}
