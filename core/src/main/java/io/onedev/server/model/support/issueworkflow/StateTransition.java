package io.onedev.server.model.support.issueworkflow;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import io.onedev.server.model.support.issueworkflow.action.IssueAction;
import io.onedev.server.util.editable.annotation.ChoiceProvider;
import io.onedev.server.util.editable.annotation.Editable;
import io.onedev.server.web.page.project.setting.issueworkflow.IssueWorkflowPage;
import io.onedev.server.web.util.WicketUtils;

@Editable
public class StateTransition implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	private List<String> fromStates;
	
	private List<String> toStates;
	
	private TransitionPrerequisite prerequisite;
	
	private IssueAction onAction;
	
	@Editable(order=100)
	@Size(min=1, message="At least one state needs to be specified")
	@ChoiceProvider("getStateChoices")
	public List<String> getFromStates() {
		return fromStates;
	}

	public void setFromStates(List<String> fromStates) {
		this.fromStates = fromStates;
	}

	@Editable(order=200)
	@Size(min=1, message="At least one state needs to be specified")
	@ChoiceProvider("getStateChoices")
	public List<String> getToStates() {
		return toStates;
	}

	public void setToStates(List<String> toStates) {
		this.toStates = toStates;
	}

	@Editable(order=300, description="Enable if applicability of this transition depends on "
			+ "value of particular field")
	public TransitionPrerequisite getPrerequisite() {
		return prerequisite;
	}

	public void setPrerequisite(TransitionPrerequisite prerequisite) {
		this.prerequisite = prerequisite;
	}

	@Editable(order=400, description="Do the transition when")
	@NotNull
	public IssueAction getOnAction() {
		return onAction;
	}

	public void setOnAction(IssueAction onAction) {
		this.onAction = onAction;
	}

	@SuppressWarnings("unused")
	private static List<String> getStateChoices() {
		IssueWorkflowPage page = (IssueWorkflowPage) WicketUtils.getPage();
		List<String> stateNames = new ArrayList<>();
		for (StateSpec state: page.getWorkflow().getStates())
			stateNames.add(state.getName());
		return stateNames;
	}
	
}
