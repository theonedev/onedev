package io.onedev.server.model.support.issue.workflow;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.hibernate.validator.constraints.NotEmpty;

import io.onedev.server.model.support.authorized.SpecifiedGroup;
import io.onedev.server.model.support.authorized.SpecifiedUser;
import io.onedev.server.model.support.issue.workflow.action.IssueAction;
import io.onedev.server.model.support.issue.workflow.action.PressButton;
import io.onedev.server.model.support.issue.workflow.transitionprerequisite.TransitionPrerequisite;
import io.onedev.server.util.editable.annotation.ChoiceProvider;
import io.onedev.server.util.editable.annotation.Editable;
import io.onedev.server.util.editable.annotation.NameOfEmptyValue;
import io.onedev.server.web.page.project.setting.issueworkflow.IssueWorkflowPage;
import io.onedev.server.web.util.WicketUtils;
import io.onedev.utils.StringUtils;

@Editable
public class StateTransition implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	private List<String> fromStates;
	
	private String toState;
	
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
	@NotEmpty
	@ChoiceProvider("getStateChoices")
	public String getToState() {
		return toState;
	}

	public void setToState(String toState) {
		this.toState = toState;
	}

	@Editable(order=300, description="Enable if applicability of this transition depends on "
			+ "value of particular field")
	@NameOfEmptyValue("No prerequisite")
	public TransitionPrerequisite getPrerequisite() {
		return prerequisite;
	}

	public void setPrerequisite(TransitionPrerequisite prerequisite) {
		this.prerequisite = prerequisite;
	}

	@Editable(order=400, name="Do Transition When")
	@NotNull(message="may not be empty")
	public IssueAction getOnAction() {
		return onAction;
	}

	public void setOnAction(IssueAction onAction) {
		this.onAction = onAction;
	}
	
	public void onRenameUser(String oldName, String newName) {
		IssueAction onAction = getOnAction();
		if (onAction instanceof PressButton) {
			PressButton pressButton = (PressButton) onAction;
			if (pressButton.getAuthorized() instanceof SpecifiedUser) {
				SpecifiedUser specifiedUser = (SpecifiedUser) pressButton.getAuthorized();
				if (specifiedUser.getUserName().equals(oldName))
					specifiedUser.setUserName(newName);
			}
		}
	}
	
	public List<String> onDeleteUser(String userName) {
		List<String> usages = new ArrayList<>();
		IssueAction onAction = getOnAction();
		if (onAction instanceof PressButton) {
			PressButton pressButton = (PressButton) onAction;
			if (pressButton.getAuthorized() instanceof SpecifiedUser) {
				SpecifiedUser specifiedUser = (SpecifiedUser) pressButton.getAuthorized();
				if (specifiedUser.getUserName().equals(userName))
					usages.add("On Action");
			}
		}
		return usages;
	}
	
	public void onRenameGroup(String oldName, String newName) {
		IssueAction onAction = getOnAction();
		if (onAction instanceof PressButton) {
			PressButton pressButton = (PressButton) onAction;
			if (pressButton.getAuthorized() instanceof SpecifiedGroup) {
				SpecifiedGroup specifiedGroup = (SpecifiedGroup) pressButton.getAuthorized();
				if (specifiedGroup.getGroupName().equals(oldName))
					specifiedGroup.setGroupName(newName);
			}
		}
	}
	
	public List<String> onDeleteGroup(String groupName) {
		List<String> usages = new ArrayList<>();
		IssueAction onAction = getOnAction();
		if (onAction instanceof PressButton) {
			PressButton pressButton = (PressButton) onAction;
			if (pressButton.getAuthorized() instanceof SpecifiedGroup) {
				SpecifiedGroup specifiedGroup = (SpecifiedGroup) pressButton.getAuthorized();
				if (specifiedGroup.getGroupName().equals(groupName))
					usages.add("On Action");
			}
		}
		return usages;
	}
	
	public void onFieldRename(String oldName, String newName) {
		if (getPrerequisite() != null && getPrerequisite().getFieldName().equals(oldName))
			getPrerequisite().setFieldName(newName);
	}
	
	public List<String> onFieldDelete(String fieldName) {
		List<String> usages = new ArrayList<>();
		if (getPrerequisite() != null && getPrerequisite().getFieldName().equals(fieldName))
			usages.add("Prerequisite");
		return usages;
	}
	
	@SuppressWarnings("unused")
	private static List<String> getStateChoices() {
		IssueWorkflowPage page = (IssueWorkflowPage) WicketUtils.getPage();
		List<String> stateNames = new ArrayList<>();
		for (StateSpec state: page.getWorkflow().getStates())
			stateNames.add(state.getName());
		return stateNames;
	}
	
	@Override
	public String toString() {
		return StringUtils.join(getFromStates()) + "-->" + getToState();		
	}
	
}
