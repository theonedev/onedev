package io.onedev.server.model.support.administration;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.Nullable;
import javax.validation.ConstraintValidatorContext;

import edu.emory.mathcs.backport.java.util.Collections;
import io.onedev.commons.utils.ExplicitException;
import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.ProjectManager;
import io.onedev.server.entitymanager.SettingManager;
import io.onedev.server.model.Project;
import io.onedev.server.model.support.issue.field.supply.FieldSupply;
import io.onedev.server.util.match.Matcher;
import io.onedev.server.util.match.StringMatcher;
import io.onedev.server.util.patternset.PatternSet;
import io.onedev.server.util.usage.Usage;
import io.onedev.server.util.validation.Validatable;
import io.onedev.server.util.validation.annotation.ClassValidating;
import io.onedev.server.web.component.issue.workflowreconcile.UndefinedFieldResolution;
import io.onedev.server.web.component.issue.workflowreconcile.UndefinedFieldValue;
import io.onedev.server.web.component.issue.workflowreconcile.UndefinedFieldValuesResolution;
import io.onedev.server.web.editable.annotation.Editable;

@Editable
@ClassValidating
public class ServiceDeskSetting implements Serializable, Validatable {

	private static final long serialVersionUID = 1L;
	
	private static final String PROP_PROJECT_DESIGNATIONS = "projectDesignations";

	private static final String PROP_ISSUE_CREATION_SETTINGS = "issueCreationSettings";
	
	private List<SenderAuthorization> senderAuthorizations = new ArrayList<>(); 
	
	private List<ProjectDesignation> projectDesignations = new ArrayList<>();
	
	private List<IssueCreationSetting> issueCreationSettings = new ArrayList<>();
	
	@Editable(order=100, description="When sender email address can not be mapped to an existing user, "
			+ "OneDev will use entries defined here to determine if the sender has permission to "
			+ "create issues. For a particular sender, the first matching entry will take "
			+ "effect")
	public List<SenderAuthorization> getSenderAuthorizations() {
		return senderAuthorizations;
	}

	public void setSenderAuthorizations(List<SenderAuthorization> senderAuthorizations) {
		this.senderAuthorizations = senderAuthorizations;
	}

	@Editable(order=200, description="When email is sent to system email address without specifying "
			+ "project information, OneDev will use entries defined here to decide in which "
			+ "project to create issues. For a particular sender, the first matching entry will "
			+ "take effect")
	public List<ProjectDesignation> getProjectDesignations() {
		return projectDesignations;
	}

	public void setProjectDesignations(List<ProjectDesignation> projectDesignations) {
		this.projectDesignations = projectDesignations;
	}

	@SuppressWarnings("unused")
	private static List<String> getProjectChoices() {
		List<String> projectNames = OneDev.getInstance(ProjectManager.class)
				.query().stream().map(it->it.getName()).collect(Collectors.toList());
		Collections.sort(projectNames);
		return projectNames;
	}
	
	@Editable(order=300, description="Specify issue creation settings. For a particular sender and project, "
			+ "the first matching entry will take effect. If no entry matches, default issue creation "
			+ "settings defined below will be used")
	public List<IssueCreationSetting> getIssueCreationSettings() {
		return issueCreationSettings;
	}

	public void setIssueCreationSettings(List<IssueCreationSetting> issueCreationSettings) {
		this.issueCreationSettings = issueCreationSettings;
	}
	
	@SuppressWarnings("unused")
	private static Collection<String> getIssueFieldNames() {
		return OneDev.getInstance(SettingManager.class).getIssueSetting().getPromptFieldsUponIssueOpen();
	}

	@Nullable
	public SenderAuthorization getSenderAuthorization(String senderAddress) {
		Matcher matcher = new StringMatcher();
		for (SenderAuthorization authorization: senderAuthorizations) {
			String patterns = authorization.getSenderEmails();
			if (patterns == null)
				patterns = "*";
			PatternSet patternSet = PatternSet.parse(patterns);
			if (patternSet.matches(matcher, senderAddress))
				return authorization;
		}
		return null;
	}
	
	public String getDesignatedProject(String senderAddress) {
		Matcher matcher = new StringMatcher();
		for (ProjectDesignation designation: projectDesignations) {
			String patterns = designation.getSenderEmails();
			if (patterns == null)
				patterns = "*";
			PatternSet patternSet = PatternSet.parse(patterns);
			if (patternSet.matches(matcher, senderAddress))
				return designation.getProject();
		}
		throw new ExplicitException("No project designated for sender: " + senderAddress);
	}
	
	public List<FieldSupply> getIssueCreationSetting(String senderAddress, Project project) {
		Matcher matcher = new StringMatcher();
		for (IssueCreationSetting setting: issueCreationSettings) {
			String senderPatterns = setting.getSenderEmails();
			if (senderPatterns == null)
				senderPatterns = "*";
			PatternSet senderPatternSet = PatternSet.parse(senderPatterns);
			
			String projectPatterns = setting.getApplicableProjects();
			if (projectPatterns == null)
				projectPatterns = "*";
			PatternSet projectPatternSet = PatternSet.parse(projectPatterns);
			
			if (senderPatternSet.matches(matcher, senderAddress) && projectPatternSet.matches(matcher, project.getName()))
				return setting.getIssueFields();
		}
		String errorMessage = String.format("No issue creation setting (sender: %s, project: %s)", 
				senderAddress, project.getName());
		throw new ExplicitException(errorMessage);
	}
	
	public void onRenameRole(String oldName, String newName) {
		for (SenderAuthorization authorization: getSenderAuthorizations()) {
			if (authorization.getAuthorizedRoleName().equals(oldName))
				authorization.setAuthorizedRoleName(newName);
		}
	}
	
	public Usage onDeleteRole(String roleName) {
		Usage usage = new Usage();
		int index = 0;
		for (SenderAuthorization authorization: getSenderAuthorizations()) {
			if (authorization.getAuthorizedRoleName().equals(roleName))
				usage.add("sender authorization #" + index + ": authorized role");
			index++;
		}
		return usage;
	}
	
	public void onRenameProject(String oldName, String newName) {
		for (SenderAuthorization authorization: getSenderAuthorizations()) {
			PatternSet patternSet = PatternSet.parse(authorization.getAuthorizedProjects());
			if (patternSet.getIncludes().remove(oldName))
				patternSet.getIncludes().add(newName);
			if (patternSet.getExcludes().remove(oldName))
				patternSet.getExcludes().add(newName);
			authorization.setAuthorizedProjects(patternSet.toString());
			if (authorization.getAuthorizedProjects().length() == 0)
				authorization.setAuthorizedProjects(null);
		}
		for (ProjectDesignation designation: getProjectDesignations()) {
			if (designation.getProject().equals(oldName))
				designation.setProject(newName);
		}
		for (IssueCreationSetting setting: getIssueCreationSettings()) {
			PatternSet patternSet = PatternSet.parse(setting.getApplicableProjects());
			if (patternSet.getIncludes().remove(oldName))
				patternSet.getIncludes().add(newName);
			if (patternSet.getExcludes().remove(oldName))
				patternSet.getExcludes().add(newName);
			setting.setApplicableProjects(patternSet.toString());
			if (setting.getApplicableProjects().length() == 0)
				setting.setApplicableProjects(null);
		}
	}
	
	public Usage onDeleteProject(String projectName) {
		Usage usage = new Usage();
		
		int index = 0;
		for (SenderAuthorization authorization: getSenderAuthorizations()) {
			PatternSet patternSet = PatternSet.parse(authorization.getAuthorizedProjects());
			if (patternSet.getIncludes().contains(projectName) || patternSet.getExcludes().contains(projectName))
				usage.add("sender authorization #" + index + ": authorized projects");
			index++;
		}
		
		index = 0;
		for (ProjectDesignation senderProject: getProjectDesignations()) {
			if (senderProject.getProject().equals(projectName))
				usage.add("sender project #" + index + ": project");
			index++;
		}
		
		index = 0;
		for (IssueCreationSetting setting: getIssueCreationSettings()) {
			PatternSet patternSet = PatternSet.parse(setting.getApplicableProjects());
			if (patternSet.getIncludes().contains(projectName) || patternSet.getExcludes().contains(projectName))
				usage.add("issue creation setting #" + index + ": applicable projects");
			index++;
		}
		return usage.prefix("service desk setting");
	}

	public Set<String> getUndefinedIssueFields() {
		Set<String> undefinedFields = new HashSet<>();
		for (IssueCreationSetting setting: getIssueCreationSettings()) 
			undefinedFields.addAll(setting.getUndefinedFields());
		return undefinedFields;
	}

	public Collection<UndefinedFieldValue> getUndefinedIssueFieldValues() {
		Collection<UndefinedFieldValue> undefinedFieldValues = new HashSet<>(); 
		for (IssueCreationSetting setting: getIssueCreationSettings()) 
			undefinedFieldValues.addAll(setting.getUndefinedFieldValues());
		return undefinedFieldValues;
	}
	
	public void fixUndefinedIssueFields( Map<String, UndefinedFieldResolution> resolutions) {
		for (IssueCreationSetting setting: getIssueCreationSettings()) 
			setting.fixUndefinedFields(resolutions);
	}

	public void fixUndefinedIssueFieldValues(Map<String, UndefinedFieldValuesResolution> resolutions) {
		for (IssueCreationSetting setting: getIssueCreationSettings()) 
			setting.fixUndefinedFieldValues(resolutions);
	}

	@Override
	public boolean isValid(ConstraintValidatorContext context) {
		boolean isValid = true;
		
		boolean foundDefault = false;
		for (ProjectDesignation designation: getProjectDesignations()) {
			if (designation.getSenderEmails() == null) {
				foundDefault = true;
				break;
			}
		}
		if (!foundDefault) {
			String errorMessage = "An entry with any sender should be defined to be used as "
					+ "default project designation"; 
			context.buildConstraintViolationWithTemplate(errorMessage)
					.addPropertyNode(PROP_PROJECT_DESIGNATIONS)
					.addConstraintViolation();
			isValid = false;
		}
		
		foundDefault = false;
		for (IssueCreationSetting setting: getIssueCreationSettings()) {
			if (setting.getSenderEmails() == null && setting.getApplicableProjects() == null) {
				foundDefault = true;
				break;
			}
		}
		if (!foundDefault) {
			String errorMessage = "An entry with any sender and any project should be defined "
					+ "to be use as default issue creation setting"; 
			context.buildConstraintViolationWithTemplate(errorMessage)
					.addPropertyNode(PROP_ISSUE_CREATION_SETTINGS)
					.addConstraintViolation();
			isValid = false;
		}
		
		if (!isValid)
			context.disableDefaultConstraintViolation();
		
		return isValid;
	}
	
}
