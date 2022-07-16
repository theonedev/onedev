package io.onedev.server.model.support.issue.transitiontrigger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import org.hibernate.validator.constraints.NotEmpty;

import edu.emory.mathcs.backport.java.util.Collections;
import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.RoleManager;
import io.onedev.server.entitymanager.SettingManager;
import io.onedev.server.model.Issue;
import io.onedev.server.model.IssueField;
import io.onedev.server.model.Membership;
import io.onedev.server.model.Project;
import io.onedev.server.model.Role;
import io.onedev.server.model.User;
import io.onedev.server.model.support.administration.GlobalIssueSetting;
import io.onedev.server.model.support.inputspec.InputSpec;
import io.onedev.server.model.support.issue.field.spec.FieldSpec;
import io.onedev.server.model.support.issue.field.spec.GroupChoiceField;
import io.onedev.server.model.support.issue.field.spec.UserChoiceField;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.util.usage.Usage;
import io.onedev.server.web.component.issue.workflowreconcile.ReconcileUtils;
import io.onedev.server.web.component.issue.workflowreconcile.UndefinedFieldResolution;
import io.onedev.server.web.editable.annotation.ChoiceProvider;
import io.onedev.server.web.editable.annotation.Editable;
import io.onedev.server.web.editable.annotation.IssueQuery;

@Editable(order=100, name="Button is pressed")
public class PressButtonTrigger extends TransitionTrigger {

	private static final long serialVersionUID = 1L;
	
	public static final String ROLE_SUBMITTER = "<Issue Submitter>";
	
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

	@Editable(order=200, description="Optionally specify authorized roles to press this button. If not specified, all users are allowed")
	@ChoiceProvider("getRoleChoices")
	public List<String> getAuthorizedRoles() {
		return authorizedRoles;
	}
	
	public void setAuthorizedRoles(List<String> authorizedRoles) {
		this.authorizedRoles = authorizedRoles;
	}
	
	@SuppressWarnings("unused")
	private static List<String> getRoleChoices() {
		List<String> choices = new ArrayList<>();
		for (Role role: OneDev.getInstance(RoleManager.class).query()) 
			choices.add(role.getName());
		Collections.sort(choices);
		choices.add(ROLE_SUBMITTER);
		for (FieldSpec field: getIssueSetting().getFieldSpecs()) {
			if (field instanceof UserChoiceField || field instanceof GroupChoiceField)
				choices.add("{" + field.getName()+ "}");
		}
		return choices;
	}

	@Editable(order=500, placeholder="No fields to prompt", description="Optionally select fields "
			+ "to prompt when this button is pressed")
	@ChoiceProvider("getFieldChoices")
	public List<String> getPromptFields() {
		return promptFields;
	}

	public void setPromptFields(List<String> promptFields) {
		this.promptFields = promptFields;
	}
	
	private static GlobalIssueSetting getIssueSetting() {
		return OneDev.getInstance(SettingManager.class).getIssueSetting();
	}
	
	@SuppressWarnings("unused")
	private static List<String> getFieldChoices() {
		List<String> fields = new ArrayList<>();
		for (FieldSpec field: getIssueSetting().getFieldSpecs())
			fields.add(field.getName());
		return fields;
	}

	@Nullable
	private String getFieldName(String roleName) {
		if (roleName.startsWith("{") && roleName.endsWith("}")) {
			String fieldName = roleName.substring(1);
			return fieldName.substring(0, fieldName.length()-1);
		} else {
			return null;
		}
	}

	@Override
	public Collection<String> getUndefinedFields() {
		Collection<String> undefinedFields = new ArrayList<>();
		GlobalIssueSetting setting = getIssueSetting();
		for (String field: getPromptFields()) {
			if (setting.getFieldSpec(field) == null)
				undefinedFields.add(field);
		}
		for (String roleName: getAuthorizedRoles()) {
			String fieldName = getFieldName(roleName);
			if (fieldName != null && setting.getFieldSpec(fieldName) == null)
				undefinedFields.add(fieldName);
		}
		
		return undefinedFields;
	}

	@Override
	public boolean fixUndefinedFields(Map<String, UndefinedFieldResolution> resolutions) {
		for (Map.Entry<String, UndefinedFieldResolution> entry: resolutions.entrySet()) {
			if (entry.getValue().getFixType() == UndefinedFieldResolution.FixType.CHANGE_TO_ANOTHER_FIELD) { 
				ReconcileUtils.renameItem(getPromptFields(), entry.getKey(), entry.getValue().getNewField());
				ReconcileUtils.renameItem(getAuthorizedRoles(), "{" + entry.getKey() + "}", "{" + entry.getValue().getNewField() + "}");
			} else { 
				getPromptFields().remove(entry.getKey());
				getAuthorizedRoles().remove("{" + entry.getKey() + "}");
			}
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
	
	public boolean isAuthorized(Issue issue) {
		User user = SecurityUtils.getUser();
		Project project = issue.getProject();
		if (user != null) {
			if (!getAuthorizedRoles().isEmpty()) {
				if (SecurityUtils.canManageIssues(project)) {
					return true;
				} else {
					for (String roleName: getAuthorizedRoles()) {
						String fieldName = getFieldName(roleName);
						if (fieldName != null) {
							for (IssueField field: issue.getFields()) {
								if (field.getName().equals(fieldName)) {
									if (field.getType().equals(InputSpec.USER)) {
										if (user.getName().equals(field.getValue()))
											return true;
									} else if (field.getType().equals(InputSpec.GROUP)) {
										for (Membership membership: user.getMemberships()) {
											if (membership.getGroup().getName().equals(field.getValue()))
												return true;
										}
									}
								}
							}
						} else if (roleName.equals(ROLE_SUBMITTER)) {
							if (user.equals(issue.getSubmitter()))
								return true;
						} else if (SecurityUtils.isAuthorizedWithRole(project, roleName)) {
							return true;
						}
					}
					return false;
				}
			} else {
				return true;
			}
		} else {
			return false;
		}
	}

	@Editable(order=1000, name="Applicable Issues", placeholder="All", description="Optionally specify "
			+ "issues applicable for this transition. Leave empty for all issues")
	@IssueQuery(withOrder = false, withCurrentUserCriteria = true)	
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
			return "button '" + buttonLabel + "' is pressed by any user";
		else
			return "button '" + buttonLabel + "' is pressed by any user of roles " + authorizedRoles;
	}
	
}
