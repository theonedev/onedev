package io.onedev.server.model.support.administration;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nullable;

import io.onedev.commons.utils.ExplicitException;
import io.onedev.server.model.Project;
import io.onedev.server.util.match.Matcher;
import io.onedev.server.util.match.StringMatcher;
import io.onedev.server.util.patternset.PatternSet;
import io.onedev.server.util.usage.Usage;
import io.onedev.server.web.component.issue.workflowreconcile.UndefinedFieldResolution;
import io.onedev.server.web.component.issue.workflowreconcile.UndefinedFieldValue;
import io.onedev.server.web.component.issue.workflowreconcile.UndefinedFieldValuesResolution;
import io.onedev.server.web.editable.annotation.Editable;

@Editable
public class ServiceDeskSetting implements Serializable {

	private static final long serialVersionUID = 1L;

	private List<SenderAuthorization> senderAuthorizations = new ArrayList<>(); 
	
	private List<DefaultProjectDesignation> defaultProjectDesignations = new ArrayList<>();
	
	private List<IssueCreationSetting> issueCreationSettings = new ArrayList<>();

	public List<SenderAuthorization> getSenderAuthorizations() {
		return senderAuthorizations;
	}

	public void setSenderAuthorizations(List<SenderAuthorization> senderAuthorizations) {
		this.senderAuthorizations = senderAuthorizations;
	}

	public List<DefaultProjectDesignation> getDefaultProjectDesignations() {
		return defaultProjectDesignations;
	}

	public void setDefaultProjectDesignations(List<DefaultProjectDesignation> defaultProjectDesignations) {
		this.defaultProjectDesignations = defaultProjectDesignations;
	}

	public List<IssueCreationSetting> getIssueCreationSettings() {
		return issueCreationSettings;
	}

	public void setIssueCreationSettings(List<IssueCreationSetting> issueCreationSettings) {
		this.issueCreationSettings = issueCreationSettings;
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
	
	@Nullable
	public DefaultProjectDesignation getDefaultProjectDesignation(String senderAddress) {
		Matcher matcher = new StringMatcher();
		for (DefaultProjectDesignation designation: defaultProjectDesignations) {
			String patterns = designation.getSenderEmails();
			if (patterns == null)
				patterns = "*";
			PatternSet patternSet = PatternSet.parse(patterns);
			if (patternSet.matches(matcher, senderAddress))
				return designation;
		}
		return null;
	}
	
	public IssueCreationSetting getIssueCreationSetting(String senderAddress, Project project) {
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
				return setting;
		}
		String errorMessage = String.format("No issue creation setting found (sender: %s, project: %s)", 
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
		for (DefaultProjectDesignation designation: getDefaultProjectDesignations()) {
			if (designation.getDefaultProject().equals(oldName))
				designation.setDefaultProject(newName);
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
		for (DefaultProjectDesignation designation: getDefaultProjectDesignations()) {
			if (designation.getDefaultProject().equals(projectName))
				usage.add("default project designation #" + index + ": default project");
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
	
}
