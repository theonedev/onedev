package io.onedev.server.model.support.issue.workflow;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.hibernate.validator.constraints.NotEmpty;

import io.onedev.server.model.Issue;
import io.onedev.server.model.support.authorized.SpecifiedGroup;
import io.onedev.server.model.support.authorized.SpecifiedUser;
import io.onedev.server.model.support.issue.IssueField;
import io.onedev.server.model.support.issue.workflow.transitionprerequisite.TransitionPrerequisite;
import io.onedev.server.model.support.issue.workflow.transitiontrigger.TransitionTrigger;
import io.onedev.server.model.support.issue.workflow.transitiontrigger.BuildSuccessfulTrigger;
import io.onedev.server.model.support.issue.workflow.transitiontrigger.PressButtonTrigger;
import io.onedev.server.model.support.issue.workflow.transitiontrigger.PullRequestTrigger;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.web.editable.annotation.ChoiceProvider;
import io.onedev.server.web.editable.annotation.Editable;
import io.onedev.server.web.editable.annotation.Multiline;
import io.onedev.server.web.editable.annotation.NameOfEmptyValue;
import io.onedev.server.web.page.project.setting.issueworkflow.IssueWorkflowPage;
import io.onedev.server.web.util.WicketUtils;
import io.onedev.utils.StringUtils;

@Editable
public class TransitionSpec implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	private String description;
	
	private List<String> fromStates;
	
	private String toState;
	
	private TransitionPrerequisite prerequisite;
	
	private TransitionTrigger trigger;
	
	@Editable(order=50)
	@NameOfEmptyValue("No description")
	@Multiline
	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
	
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
	public TransitionTrigger getTrigger() {
		return trigger;
	}

	public void setTrigger(TransitionTrigger trigger) {
		this.trigger = trigger;
	}
	
	public void onRenameUser(String oldName, String newName) {
		TransitionTrigger trigger = getTrigger();
		if (trigger instanceof PressButtonTrigger) {
			PressButtonTrigger pressButton = (PressButtonTrigger) trigger;
			if (pressButton.getAuthorized() instanceof SpecifiedUser) {
				SpecifiedUser specifiedUser = (SpecifiedUser) pressButton.getAuthorized();
				if (specifiedUser.getUserName().equals(oldName))
					specifiedUser.setUserName(newName);
			}
		}
	}
	
	public boolean onDeleteUser(String userName) {
		TransitionTrigger trigger = getTrigger();
		if (trigger instanceof PressButtonTrigger) {
			PressButtonTrigger pressButton = (PressButtonTrigger) trigger;
			if (pressButton.getAuthorized() instanceof SpecifiedUser) {
				SpecifiedUser specifiedUser = (SpecifiedUser) pressButton.getAuthorized();
				if (specifiedUser.getUserName().equals(userName))
					return true;
			}
		}
		return false;
	}
	
	public boolean onDeleteConfiguration(String configurationName) {
		TransitionTrigger trigger = getTrigger();
		if (trigger instanceof BuildSuccessfulTrigger) {
			BuildSuccessfulTrigger buildSuccessfulTrigger = (BuildSuccessfulTrigger) trigger;
			if (buildSuccessfulTrigger.getConfiguration().equals(configurationName))
				return true;
		}
		return false;
	}
	
	public void onRenameConfiguration(String oldName, String newName) {
		TransitionTrigger trigger = getTrigger();
		if (trigger instanceof BuildSuccessfulTrigger) {
			BuildSuccessfulTrigger buildSuccessfulTrigger = (BuildSuccessfulTrigger) trigger;
			if (buildSuccessfulTrigger.getConfiguration().equals(oldName))
				buildSuccessfulTrigger.setConfiguration(newName);
		}
	}
	
	public void onRenameGroup(String oldName, String newName) {
		TransitionTrigger trigger = getTrigger();
		if (trigger instanceof PressButtonTrigger) {
			PressButtonTrigger pressButton = (PressButtonTrigger) trigger;
			if (pressButton.getAuthorized() instanceof SpecifiedGroup) {
				SpecifiedGroup specifiedGroup = (SpecifiedGroup) pressButton.getAuthorized();
				if (specifiedGroup.getGroupName().equals(oldName))
					specifiedGroup.setGroupName(newName);
			}
		}
	}
	
	public boolean onDeleteGroup(String groupName) {
		TransitionTrigger trigger = getTrigger();
		if (trigger instanceof PressButtonTrigger) {
			PressButtonTrigger pressButton = (PressButtonTrigger) trigger;
			if (pressButton.getAuthorized() instanceof SpecifiedGroup) {
				SpecifiedGroup specifiedGroup = (SpecifiedGroup) pressButton.getAuthorized();
				if (specifiedGroup.getGroupName().equals(groupName))
					return true;
			}
		}
		return false;
	}
	
	public void onFieldRename(String oldName, String newName) {
		if (getPrerequisite() != null && getPrerequisite().getInputName().equals(oldName))
			getPrerequisite().setInputName(newName);
		if (getTrigger() instanceof BuildSuccessfulTrigger) {
			BuildSuccessfulTrigger trigger = (BuildSuccessfulTrigger) getTrigger();
			if (oldName.equals(trigger.getBuildField()))
				trigger.setBuildField(newName);
		} else if (getTrigger() instanceof PullRequestTrigger) {
			PullRequestTrigger trigger = (PullRequestTrigger) getTrigger();
			if (oldName.equals(trigger.getPullRequestField()))
				trigger.setPullRequestField(newName);
		}
	}
	
	public boolean onFieldDelete(String fieldName) {
		if (getPrerequisite() != null && getPrerequisite().getInputName().equals(fieldName)) 
			return true;
		if (getTrigger() instanceof BuildSuccessfulTrigger) {
			BuildSuccessfulTrigger trigger = (BuildSuccessfulTrigger) getTrigger();
			if (fieldName.equals(trigger.getBuildField()))
				return true;
		} else if (getTrigger() instanceof PullRequestTrigger) {
			PullRequestTrigger trigger = (PullRequestTrigger) getTrigger();
			if (fieldName.equals(trigger.getPullRequestField()))
				return true;
		}
		return false;
	}
	
	@SuppressWarnings("unused")
	private static List<String> getStateChoices() {
		IssueWorkflowPage page = (IssueWorkflowPage) WicketUtils.getPage();
		List<String> stateNames = new ArrayList<>();
		for (StateSpec state: page.getWorkflow().getStateSpecs())
			stateNames.add(state.getName());
		return stateNames;
	}
	
	@Override
	public String toString() {
		return StringUtils.join(getFromStates()) + "-->" + getToState();		
	}

	public boolean canApplyTo(Issue issue) {
		if (getFromStates().contains(issue.getState()) && getTrigger().getButton() != null && SecurityUtils.getUser() != null
				&& getTrigger().getButton().getAuthorized().matches(issue.getProject(), SecurityUtils.getUser())) {
			if (getPrerequisite() == null) {
				return true;
			} else {
				IssueField field = issue.getFields().get(getPrerequisite().getInputName());
				List<String> fieldValues;
				if (field != null)
					fieldValues = field.getValues();
				else
					fieldValues = new ArrayList<>();
				if (getPrerequisite().matches(fieldValues))
					return true;
			}
		}
		return false;
	}
	
}
