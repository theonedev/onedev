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

import edu.emory.mathcs.backport.java.util.Collections;
import io.onedev.commons.utils.ExplicitException;
import io.onedev.commons.utils.PathUtils;
import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.ProjectManager;
import io.onedev.server.model.Project;
import io.onedev.server.util.match.Matcher;
import io.onedev.server.util.match.PathMatcher;
import io.onedev.server.util.match.StringMatcher;
import io.onedev.server.util.patternset.PatternSet;
import io.onedev.server.util.usage.Usage;
import io.onedev.server.web.component.issue.workflowreconcile.UndefinedFieldResolution;
import io.onedev.server.web.component.issue.workflowreconcile.UndefinedFieldValue;
import io.onedev.server.web.component.issue.workflowreconcile.UndefinedFieldValuesResolution;
import io.onedev.server.annotation.Editable;

@Editable
public class ServiceDeskSetting implements Serializable {

	private static final long serialVersionUID = 1L;
	
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

	@Editable(order=200, name="Default Projects", description="When email is sent to system email " +
			"address without specifying project information, OneDev will use entries defined here " +
			"to decide in which project to create issues. For a particular sender, the first " +
			"matching entry will take effect")
	public List<ProjectDesignation> getProjectDesignations() {
		return projectDesignations;
	}

	public void setProjectDesignations(List<ProjectDesignation> projectDesignations) {
		this.projectDesignations = projectDesignations;
	}

	@SuppressWarnings("unused")
	private static List<String> getProjectChoices() {
		List<String> projectPaths = OneDev.getInstance(ProjectManager.class)
				.query().stream().map(it->it.getPath()).collect(Collectors.toList());
		Collections.sort(projectPaths);
		return projectPaths;
	}
	
	@Editable(order=300, description="Specify issue creation settings. For a particular sender and project, "
			+ "the first matching entry will take effect")
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
		return null;
	}
	
	public IssueCreationSetting getIssueCreationSetting(String senderAddress, Project project) {
		for (IssueCreationSetting setting: issueCreationSettings) {
			String senderPatterns = setting.getSenderEmails();
			if (senderPatterns == null)
				senderPatterns = "*";
			PatternSet senderPatternSet = PatternSet.parse(senderPatterns);
			
			String projectPatterns = setting.getApplicableProjects();
			if (projectPatterns == null)
				projectPatterns = "**";
			PatternSet projectPatternSet = PatternSet.parse(projectPatterns);
			
			if (senderPatternSet.matches(new StringMatcher(), senderAddress) 
					&& projectPatternSet.matches(new PathMatcher(), project.getPath())) {
				return setting;
			}
		}
		String errorMessage = String.format("No issue creation setting (sender: %s, project: %s)", 
				senderAddress, project.getPath());
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
		int index = 1;
		for (SenderAuthorization authorization: getSenderAuthorizations()) {
			if (authorization.getAuthorizedRoleName().equals(roleName))
				usage.add("sender authorization #" + index + ": authorized role");
			index++;
		}
		return usage;
	}
	
	public void onMoveProject(String oldPath, String newPath) {
		for (SenderAuthorization authorization: getSenderAuthorizations()) {
			authorization.setAuthorizedProjects(Project.substitutePath(
					authorization.getAuthorizedProjects(), oldPath, newPath));
		}
		for (ProjectDesignation designation: getProjectDesignations()) {
			designation.setProject(PathUtils.substituteSelfOrAncestor(
					designation.getProject(), oldPath, newPath));
		}
		for (IssueCreationSetting setting: getIssueCreationSettings()) {
			setting.setApplicableProjects(Project.substitutePath(
					setting.getApplicableProjects(), oldPath, newPath));
		}
	}
	
	public Usage onDeleteProject(String projectPath) {
		Usage usage = new Usage();
		
		int index = 1;
		for (SenderAuthorization authorization: getSenderAuthorizations()) {
			if (Project.containsPath(authorization.getAuthorizedProjects(), projectPath))
				usage.add("sender authorization #" + index + ": authorized projects");
			index++;
		}
		
		index = 1;
		for (ProjectDesignation senderProject: getProjectDesignations()) {
			if (PathUtils.isSelfOrAncestor(projectPath, senderProject.getProject()))
				usage.add("sender project #" + index + ": project");
			index++;
		}
		
		index = 1;
		for (IssueCreationSetting setting: getIssueCreationSettings()) {
			if (Project.containsPath(setting.getApplicableProjects(), projectPath))
				usage.add("issue creation setting #" + index + ": applicable projects");
			index++;
		}
		return usage.prefix("service desk settings");
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
