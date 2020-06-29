package io.onedev.server.web.page.admin.issuesetting.fieldspec;

import java.io.Serializable;

import javax.validation.constraints.NotNull;

import io.onedev.server.model.support.issue.fieldspec.FieldSpec;
import io.onedev.server.web.editable.annotation.Editable;

@Editable
public class FieldBean implements Serializable {

	private static final long serialVersionUID = 1L;

	private FieldSpec field;

	private boolean promptUponIssueOpen = true;

	@Editable(name="Type", order=100)
	@NotNull(message="may not be empty")
	public FieldSpec getField() {
		return field;
	}

	public void setField(FieldSpec field) {
		this.field = field;
	}

	@Editable(order=200, description="If checked, this field will be prompted for user input by default when issue is opened")
	public boolean isPromptUponIssueOpen() {
		return promptUponIssueOpen;
	}

	public void setPromptUponIssueOpen(boolean promptUponIssueOpen) {
		this.promptUponIssueOpen = promptUponIssueOpen;
	}

}
