package io.onedev.server.model.support.administration;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import javax.validation.Valid;

import org.hibernate.validator.constraints.NotEmpty;

import edu.emory.mathcs.backport.java.util.Collections;
import io.onedev.commons.codeassist.InputSuggestion;
import io.onedev.commons.utils.ExplicitException;
import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.ProjectManager;
import io.onedev.server.entitymanager.RoleManager;
import io.onedev.server.entitymanager.SettingManager;
import io.onedev.server.model.Project;
import io.onedev.server.model.Role;
import io.onedev.server.model.support.issue.field.supply.FieldSupply;
import io.onedev.server.util.match.Matcher;
import io.onedev.server.util.match.StringMatcher;
import io.onedev.server.util.patternset.PatternSet;
import io.onedev.server.web.editable.annotation.ChoiceProvider;
import io.onedev.server.web.editable.annotation.Editable;
import io.onedev.server.web.editable.annotation.FieldNamesProvider;
import io.onedev.server.web.editable.annotation.NameOfEmptyValue;
import io.onedev.server.web.editable.annotation.OmitName;
import io.onedev.server.web.editable.annotation.Patterns;
import io.onedev.server.web.editable.annotation.RoleChoice;
import io.onedev.server.web.util.SuggestionUtils;

@Editable
public class SenderAuthorization implements Serializable {

	private static final long serialVersionUID = 1L;

	private String senderEmails;
	
	private String authorizedProjects;
	
	private String authorizedRoleName;
	
	private String defaultProject;

	private List<FieldSupply> issueFields = new ArrayList<>();

	@Editable(order=100, name="Applicable Senders", description="Specify space-separated sender "
			+ "email addresses applicable for this entry. Use '*' or '?' for wildcard match. "
			+ "Prefix with '-' to exclude. Leave empty to match all senders")
	@Patterns
	@NameOfEmptyValue("Any sender")
	public String getSenderEmails() {
		return senderEmails;
	}

	public void setSenderEmails(String senderEmails) {
		this.senderEmails = senderEmails;
	}

	@Editable(order=150, description="Specify space-separated projects authorized to senders above. "
			+ "Use '*' or '?' for wildcard match. Prefix with '-' to exclude. Leave empty to "
			+ "authorize all projects")
	@Patterns(suggester="suggestProjects")
	@NameOfEmptyValue("Any project")
	public String getAuthorizedProjects() {
		return authorizedProjects;
	}

	public void setAuthorizedProjects(String authorizedProjects) {
		this.authorizedProjects = authorizedProjects;
	}
	
	@SuppressWarnings("unused")
	private static List<InputSuggestion> suggestProjects(String matchWith) {
		return SuggestionUtils.suggestProjects(matchWith);
	}
	
	@Editable(order=175, name="Authorized Role", description="Specify authorized role for above projects")
	@RoleChoice
	@NotEmpty
	public String getAuthorizedRoleName() {
		return authorizedRoleName;
	}

	public void setAuthorizedRoleName(String authorizedRoleName) {
		this.authorizedRoleName = authorizedRoleName;
	}
	
	public Role getAuthorizedRole() {
		Role role = OneDev.getInstance(RoleManager.class).find(authorizedRoleName);
		if (role == null)
			throw new ExplicitException("Undefined role: " + authorizedRoleName);
		return role;
	}
	
	@Editable(order=200, description="Upon receiving new email (aka, not replying to notification emails), OneDev "
			+ "will check subaddress of the email to determine which project to create issue in. If subadddress "
			+ "is not specified, this property will be used to determine project based on sender address")
	@ChoiceProvider("getProjectChoices")
	@NotEmpty
	public String getDefaultProject() {
		return defaultProject;
	}

	public void setDefaultProject(String defaultProject) {
		this.defaultProject = defaultProject;
	}

	@SuppressWarnings("unused")
	private static List<String> getProjectChoices() {
		List<String> projectNames = OneDev.getInstance(ProjectManager.class)
				.query().stream().map(it->it.getName()).collect(Collectors.toList());
		Collections.sort(projectNames);
		return projectNames;
	}
	
	@Editable(order=300)
	@FieldNamesProvider("getFieldNames")
	@OmitName
	@Valid
	public List<FieldSupply> getIssueFields() {
		return issueFields;
	}

	public void setIssueFields(List<FieldSupply> issueFields) {
		this.issueFields = issueFields;
	}
	
	@SuppressWarnings("unused")
	private static Collection<String> getFieldNames() {
		return OneDev.getInstance(SettingManager.class).getIssueSetting().getPromptFieldsUponIssueOpen();
	}
	
	public boolean isProjectAuthorized(Project project) {
		String authorizedProjects = this.authorizedProjects;
		if (authorizedProjects == null)
			authorizedProjects = "*";
		Matcher matcher = new StringMatcher();
		return PatternSet.parse(authorizedProjects).matches(matcher, project.getName());
	}
	
}