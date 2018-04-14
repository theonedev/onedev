package io.onedev.server.web.page.project.issues.issuelist.workflowreconcile;

import java.io.Serializable;
import java.util.List;
import java.util.stream.Collectors;

import org.hibernate.validator.constraints.NotEmpty;

import io.onedev.server.util.editable.annotation.ChoiceProvider;
import io.onedev.server.util.editable.annotation.Editable;
import io.onedev.server.util.editable.annotation.OmitName;
import io.onedev.server.web.page.project.ProjectPage;
import io.onedev.server.web.util.WicketUtils;

@Editable
public class UndefinedStateResolution implements Serializable {

	private static final long serialVersionUID = 1L;

	private String newState;

	@Editable
	@ChoiceProvider("getNewStateChoices")
	@OmitName
	@NotEmpty
	public String getNewState() {
		return newState;
	}

	public void setNewState(String newState) {
		this.newState = newState;
	}
	
	@SuppressWarnings("unused")
	private static List<String> getNewStateChoices() {
		ProjectPage projectPage = (ProjectPage) WicketUtils.getPage();
		return projectPage.getProject().getIssueWorkflow().getStates().stream().map(each->each.getName()).collect(Collectors.toList());
	}
	
}
