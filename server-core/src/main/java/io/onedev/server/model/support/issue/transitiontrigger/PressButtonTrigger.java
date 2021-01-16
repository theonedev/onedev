package io.onedev.server.model.support.issue.transitiontrigger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.hibernate.validator.constraints.NotEmpty;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.SettingManager;
import io.onedev.server.model.Project;
import io.onedev.server.model.support.administration.GlobalIssueSetting;
import io.onedev.server.model.support.issue.fieldspec.FieldSpec;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.util.usage.Usage;
import io.onedev.server.web.component.issue.workflowreconcile.ReconcileUtils;
import io.onedev.server.web.component.issue.workflowreconcile.UndefinedFieldResolution;
import io.onedev.server.web.editable.annotation.ChoiceProvider;
import io.onedev.server.web.editable.annotation.Editable;
import io.onedev.server.web.editable.annotation.IssueQuery;
import io.onedev.server.web.editable.annotation.NameOfEmptyValue;
import io.onedev.server.web.editable.annotation.RoleChoice;

@Editable(order=100, name="Button is pressed")
public class PressButtonTrigger extends TransitionTrigger {

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
		GlobalIssueSetting issueSetting = OneDev.getInstance(SettingManager.class).getIssueSetting();
		for (FieldSpec field: issueSetting.getFieldSpecs())
			fields.add(field.getName());
		return fields;
	}

	@Override
	public Collection<String> getUndefinedFields() {
		Collection<String> undefinedFields = super.getUndefinedFields();
		GlobalIssueSetting setting = OneDev.getInstance(SettingManager.class).getIssueSetting();
		for (String field: getPromptFields()) {
			if (setting.getFieldSpec(field) == null)
				undefinedFields.add(field);
		}
		return undefinedFields;
	}

	@Override
	public boolean fixUndefinedFields(Map<String, UndefinedFieldResolution> resolutions) {
		if (!super.fixUndefinedFields(resolutions))
			return false;
		for (Map.Entry<String, UndefinedFieldResolution> entry: resolutions.entrySet()) {
			if (entry.getValue().getFixType() == UndefinedFieldResolution.FixType.CHANGE_TO_ANOTHER_FIELD) 
				ReconcileUtils.renameItem(getPromptFields(), entry.getKey(), entry.getValue().getNewField());
			else 
				getPromptFields().remove(entry.getKey());
		}
		return true;
	}

	@Override
	public void onRenameRole(String oldName, String newName) {
		int index = getAuthorizedRoles().indexOf(oldName);
		if (index != -1) 
			getAuthorizedRoles().set(index, newName);
	}

	@Override
	public Usage onDeleteRole(String roleName) {
		Usage usage = super.onDeleteRole(roleName);
		if (getAuthorizedRoles().contains(roleName))
			usage.add("authorized roles");
		return usage;
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

	@Editable(order=1000, name="Applicable Issues", description="Optionally specify issues applicable for this transition. Leave empty for all issues")
	@IssueQuery(withOrder = false, withCurrentUserCriteria = true, withCurrentBuildCriteria = false, 
			withCurrentPullRequestCriteria = false, withCurrentCommitCriteria = false)	
	@NameOfEmptyValue("All")
	@Override
	public String getIssueQuery() {
		return super.getIssueQuery();
	}

	public void setIssueQuery(String issueQuery) {
		super.setIssueQuery(issueQuery);
	}
	
	@Override
	public String getDescription() {
		if (authorizedRoles.isEmpty())
			return "Button '" + buttonLabel + "' is pressed by any user";
		else
			return "Button '" + buttonLabel + "' is pressed by any user of roles " + authorizedRoles;
	}
	
}
