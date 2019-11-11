package io.onedev.server.model.support.issue.transitiontrigger;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.validator.constraints.NotEmpty;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.SettingManager;
import io.onedev.server.model.Project;
import io.onedev.server.model.support.administration.IssueSetting;
import io.onedev.server.model.support.issue.fieldspec.FieldSpec;
import io.onedev.server.util.SecurityUtils;
import io.onedev.server.web.editable.annotation.ChoiceProvider;
import io.onedev.server.web.editable.annotation.Editable;
import io.onedev.server.web.editable.annotation.NameOfEmptyValue;
import io.onedev.server.web.editable.annotation.RoleChoice;

@Editable(order=100, name="Button is pressed")
public class PressButtonTrigger implements TransitionTrigger {

	private static final long serialVersionUID = 1L;

	private String buttonLabel;

	private List<String> authorizedRoles = new ArrayList<>();
	
	private List<String> promptFields = new ArrayList<>();
	
	@Editable(order=100)
	@NotEmpty
	public String getButtonLabel() {
		return buttonLabel;
	}

	public void setButtonLabel(String buttonLabel) {
		this.buttonLabel = buttonLabel;
	}

	@Editable(order=200, description="Optionally specify authorized roles to press this button. "
			+ "If not specified, all users are allowed")
	@RoleChoice
	public List<String> getAuthorizedRoles() {
		return authorizedRoles;
	}
	
	public void setAuthorizedRoles(List<String> authorizedRoles) {
		this.authorizedRoles = authorizedRoles;
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
		IssueSetting issueSetting = OneDev.getInstance(SettingManager.class).getIssueSetting();
		for (FieldSpec field: issueSetting.getFieldSpecs())
			fields.add(field.getName());
		return fields;
	}

	public boolean isAuthorized(Project project) {
		if (!getAuthorizedRoles().isEmpty()) {
			if (SecurityUtils.canManageIssues(Project.get())) {
				return true;
			} else {
				for (String roleName: getAuthorizedRoles()) {
					if (SecurityUtils.isAuthorizedWithRole(project, roleName))
						return true;
				}
				return false;
			}
		} else {
			return true;
		}
	}
	
}
