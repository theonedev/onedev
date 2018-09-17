package io.onedev.server.model.support.issue.workflow.transitiontrigger;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.NotEmpty;

import io.onedev.server.model.support.usermatcher.UserMatcher;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.util.inputspec.InputSpec;
import io.onedev.server.web.editable.annotation.ChoiceProvider;
import io.onedev.server.web.editable.annotation.Editable;
import io.onedev.server.web.editable.annotation.NameOfEmptyValue;
import io.onedev.server.web.page.project.ProjectPage;
import io.onedev.server.web.page.project.setting.issueworkflow.IssueWorkflowPage;
import io.onedev.server.web.util.WicketUtils;

@Editable(order=100, name="Button is pressed")
public class PressButtonTrigger implements TransitionTrigger {

	private static final long serialVersionUID = 1L;

	private String buttonLabel;

	private UserMatcher authorized;
	
	private List<String> promptFields = new ArrayList<>();
	
	@Editable(order=100)
	@NotEmpty
	public String getButtonLabel() {
		return buttonLabel;
	}

	public void setButtonLabel(String buttonLabel) {
		this.buttonLabel = buttonLabel;
	}

	@Editable(order=200, name="Authorization")
	@NotNull(message="may not be empty")
	public UserMatcher getAuthorized() {
		return authorized;
	}
	
	public void setAuthorized(UserMatcher authorized) {
		this.authorized = authorized;
	}

	@Editable(order=500, description="Optionally select fields to prompt when this button is pressed")
	@ChoiceProvider("getFieldChoices")
	@NameOfEmptyValue("No fields to prompt")
	public List<String> getPromptFields() {
		return promptFields;
	}

	public void setPromptFields(List<String> promptFields) {
		this.promptFields = promptFields;
	}
	
	@SuppressWarnings("unused")
	private static List<String> getFieldChoices() {
		List<String> fields = new ArrayList<>();
		IssueWorkflowPage page = (IssueWorkflowPage) WicketUtils.getPage();
		for (InputSpec field: page.getWorkflow().getFieldSpecs())
			fields.add(field.getName());
		return fields;
	}

	public void onRenameField(String oldName, String newName) {
		for (int i=0; i<getPromptFields().size(); i++) {
			if (getPromptFields().get(i).equals(oldName))
				getPromptFields().set(i, newName);
		}
	}
	
	public void onDeleteField(String fieldName) {
		for (Iterator<String> it = getPromptFields().iterator(); it.hasNext();) {
			if (it.next().equals(fieldName))
				it.remove();
		}
	}	
	
	public boolean isAuthorized() {
		ProjectPage page = (ProjectPage) WicketUtils.getPage();
		return SecurityUtils.getUser() != null && getAuthorized().matches(page.getProject(), SecurityUtils.getUser());		
	}
	
}
