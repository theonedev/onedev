package io.onedev.server.web.component.issuestate;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.validator.constraints.NotEmpty;

import io.onedev.server.model.support.issueworkflow.StateSpec;
import io.onedev.server.util.editable.annotation.ChoiceProvider;
import io.onedev.server.util.editable.annotation.Editable;
import io.onedev.server.web.page.project.ProjectPage;
import io.onedev.server.web.util.WicketUtils;

@Editable
public class StateFixOption implements Serializable {

	private static final long serialVersionUID = 1L;

	private String newState;
	
	private boolean fixAll;

	@Editable(order=100, name="Change to State", description="Select existing state to change the undefined state to")
	@ChoiceProvider("getStateChoices")
	@NotEmpty
	public String getNewState() {
		return newState;
	}

	public void setNewState(String newState) {
		this.newState = newState;
	}

	@Editable(order=200, description="Enable to fix the problem for all issues in the project, "
			+ "otherwise only fix for current issue")
	public boolean isFixAll() {
		return fixAll;
	}

	public void setFixAll(boolean fixAll) {
		this.fixAll = fixAll;
	}
	
	@SuppressWarnings("unused")
	private static List<String> getStateChoices() {
		ProjectPage page = (ProjectPage) WicketUtils.getPage();
		List<String> states = new ArrayList<>();
		for (StateSpec state: page.getProject().getIssueWorkflow().getStates())
			states.add(state.getName());
		return states;
	}
	
}
