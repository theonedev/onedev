package io.onedev.server.web.page.project.setting.issueworkflow.fields;

import java.io.Serializable;

import javax.validation.constraints.NotNull;

import io.onedev.server.util.inputspec.InputSpec;
import io.onedev.server.web.editable.annotation.Editable;

@Editable
public class FieldBean implements Serializable {

	private static final long serialVersionUID = 1L;

	private InputSpec field;

	private boolean promptUponIssueOpen;
	
	@Editable(name="Type", order=100)
	@NotNull(message="may not be empty")
	public InputSpec getField() {
		return field;
	}

	public void setField(InputSpec field) {
		this.field = field;
	}

	@Editable(order=200, description="Whether or not to prompt this field when the issue is opened. You may also "
			+ "choose to prompt the field when issue transits to other states in state transition definitions")
	public boolean isPromptUponIssueOpen() {
		return promptUponIssueOpen;
	}

	public void setPromptUponIssueOpen(boolean promptUponIssueOpen) {
		this.promptUponIssueOpen = promptUponIssueOpen;
	}
	
}
