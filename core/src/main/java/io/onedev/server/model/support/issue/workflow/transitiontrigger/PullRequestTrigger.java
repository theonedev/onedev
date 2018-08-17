package io.onedev.server.model.support.issue.workflow.transitiontrigger;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.validator.constraints.NotEmpty;

import io.onedev.server.util.inputspec.InputSpec;
import io.onedev.server.web.editable.annotation.BranchChoice;
import io.onedev.server.web.editable.annotation.ChoiceProvider;
import io.onedev.server.web.editable.annotation.Editable;
import io.onedev.server.web.page.project.ProjectPage;
import io.onedev.server.web.util.WicketUtils;

public abstract class PullRequestTrigger implements TransitionTrigger {

	private static final long serialVersionUID = 1L;

	private String branch;
	
	private String pullRequestField;

	@Editable(name="Target Branch")
	@BranchChoice
	@NotEmpty
	public String getBranch() {
		return branch;
	}

	public void setBranch(String branch) {
		this.branch = branch;
	}

	@Override
	public Button getButton() {
		return null;
	}
	
	@Editable(order=200, description="Optionally specify an issue field of \"pull request choice\" type to store the pull request information")
	@ChoiceProvider("getPullRequestFieldChoices")
	public String getPullRequestField() {
		return pullRequestField;
	}

	public void setPullRequestField(String pullRequestField) {
		this.pullRequestField = pullRequestField;
	}
	
	@SuppressWarnings("unused")
	private static List<String> getPullRequestFieldChoices() {
		List<String> choices = new ArrayList<>();
		ProjectPage page = (ProjectPage) WicketUtils.getPage();
		for (InputSpec field: page.getProject().getIssueWorkflow().getFieldSpecs()) {
			if (field.getType().equals(InputSpec.PULLREQUEST))
				choices.add(field.getName());
		}
		return choices;
	}
	
}
