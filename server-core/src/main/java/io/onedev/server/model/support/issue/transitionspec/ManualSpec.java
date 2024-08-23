package io.onedev.server.model.support.issue.transitionspec;

import edu.emory.mathcs.backport.java.util.Collections;
import io.onedev.server.OneDev;
import io.onedev.server.annotation.ChoiceProvider;
import io.onedev.server.annotation.Editable;
import io.onedev.server.annotation.IssueQuery;
import io.onedev.server.buildspecmodel.inputspec.InputSpec;
import io.onedev.server.entitymanager.RoleManager;
import io.onedev.server.model.*;
import io.onedev.server.model.support.administration.GlobalIssueSetting;
import io.onedev.server.model.support.issue.field.spec.FieldSpec;
import io.onedev.server.model.support.issue.field.spec.GroupChoiceField;
import io.onedev.server.model.support.issue.field.spec.userchoicefield.UserChoiceField;
import io.onedev.server.search.entity.issue.IssueQueryParseOption;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.util.usage.Usage;
import io.onedev.server.web.component.issue.workflowreconcile.ReconcileUtils;
import io.onedev.server.web.component.issue.workflowreconcile.UndefinedFieldResolution;
import io.onedev.server.web.component.issue.workflowreconcile.UndefinedStateResolution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

@Editable(order=100, name="Transit manually")
public class ManualSpec extends TransitionSpec {

	private static final long serialVersionUID = 1L;
	
	private static final Logger logger = LoggerFactory.getLogger(ManualSpec.class);
	
	public static final String ROLE_SUBMITTER = "<Issue Submitter>";

	private List<String> toStates = new ArrayList<>();
	
	private List<String> authorizedRoles = new ArrayList<>();
	
	private List<String> promptFields = new ArrayList<>();

	@Override
	@Editable(order=150, placeholder = "Any state")
	@ChoiceProvider("getStateChoices")
	public List<String> getToStates() {
		return toStates;
	}

	public void setToStates(List<String> toStates) {
		this.toStates = toStates;
	}
	
	@Editable(order=200, placeholder = "Any user", description="Optionally specify authorized roles to press this button. If not specified, all users are allowed")
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
	
	@Nullable
	private String getFieldName(String roleName) {
		if (roleName.startsWith("{") && roleName.endsWith("}")) {
			String fieldName = roleName.substring(1);
			return fieldName.substring(0, fieldName.length()-1);
		} else {
			return null;
		}
	}

	public boolean canTransit(Issue issue, @Nullable String state) {
		if ((state == null || getToStates().isEmpty() || getToStates().contains(state)) 
				&& (getFromStates().isEmpty() || getFromStates().contains(issue.getState())) 
				&& isAuthorized(issue)) {
			io.onedev.server.search.entity.issue.IssueQuery parsedQuery = io.onedev.server.search.entity.issue.IssueQuery.parse(issue.getProject(),
					getIssueQuery(), new IssueQueryParseOption().enableAll(true), true);
			return parsedQuery.matches(issue);
		} else {
			return false;
		}
	}

	@Override
	public Collection<String> getUndefinedStates() {
		Collection<String> undefinedStates = super.getUndefinedStates();
		for (var toState: getToStates()) {
			if (getIssueSetting().getStateSpec(toState) == null)
				undefinedStates.add(toState);
		}
		return undefinedStates;
	}
	
	@Override
	public Collection<String> getUndefinedFields() {
		Collection<String> undefinedFields = super.getUndefinedFields();
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
	public boolean fixUndefinedStates(Map<String, UndefinedStateResolution> resolutions) {
		if (!super.fixUndefinedStates(resolutions))
			return false;
		for (Map.Entry<String, UndefinedStateResolution> entry: resolutions.entrySet()) {
			if (entry.getValue().getFixType() == UndefinedStateResolution.FixType.CHANGE_TO_ANOTHER_STATE) {
				ReconcileUtils.renameItem(getToStates(), entry.getKey(), entry.getValue().getNewState());
			} else {
				getToStates().remove(entry.getKey());
				if (getToStates().isEmpty())
					return false;
			}
		}
		return true;
	}

	@Override
	public boolean fixUndefinedFields(Map<String, UndefinedFieldResolution> resolutions) {
		if (!super.fixUndefinedFields(resolutions))
			return false;
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
						} else {
							Role role = OneDev.getInstance(RoleManager.class).find(roleName);
							if (role != null) {
								if (SecurityUtils.isAssignedRole(project, role)) 
									return true;
							} else {
								logger.error("Undefined role: " + roleName);
							}
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
	public String getTriggerDescription() {
		if (authorizedRoles.isEmpty())
			return "transit manually by any user";
		else
			return "transit manually by any user of roles " + authorizedRoles;
	}
	
}
