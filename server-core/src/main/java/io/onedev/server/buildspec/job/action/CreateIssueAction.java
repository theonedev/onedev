package io.onedev.server.buildspec.job.action;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.jspecify.annotations.Nullable;
import javax.validation.Valid;
import javax.validation.ValidationException;
import javax.validation.constraints.NotEmpty;

import org.apache.shiro.subject.Subject;

import edu.emory.mathcs.backport.java.util.Collections;
import io.onedev.commons.codeassist.InputSuggestion;
import io.onedev.commons.utils.ExplicitException;
import io.onedev.server.OneDev;
import io.onedev.server.annotation.ChoiceProvider;
import io.onedev.server.annotation.DependsOn;
import io.onedev.server.annotation.Editable;
import io.onedev.server.annotation.FieldNamesProvider;
import io.onedev.server.annotation.Interpolative;
import io.onedev.server.annotation.Multiline;
import io.onedev.server.annotation.OmitName;
import io.onedev.server.buildspec.BuildSpec;
import io.onedev.server.buildspec.job.Job;
import io.onedev.server.service.IssueService;
import io.onedev.server.service.ProjectService;
import io.onedev.server.service.SettingService;
import io.onedev.server.job.JobAuthorizationContext;
import io.onedev.server.model.Build;
import io.onedev.server.model.Issue;
import io.onedev.server.model.Project;
import io.onedev.server.model.support.administration.GlobalIssueSetting;
import io.onedev.server.model.support.issue.field.FieldUtils;
import io.onedev.server.model.support.issue.field.instance.FieldInstance;
import io.onedev.server.persistence.TransactionService;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.security.permission.AccessProject;
import io.onedev.server.util.facade.ProjectCache;
import io.onedev.server.web.page.project.ProjectPage;
import io.onedev.server.web.util.WicketUtils;

@Editable(name="Create issue", order=300)
public class CreateIssueAction extends PostBuildAction {

	private static final long serialVersionUID = 1L;
	
	private String projectPath;
	
	private String accessTokenSecret;
	
	private String issueTitle;
	
	private String issueDescription;
	
	private boolean issueConfidential;
	
	private List<FieldInstance> issueFields = new ArrayList<>();

	@Editable(order=900, name="Project", placeholder = "Current project", description="Optionally Specify project to create issue in. " +
			"Leave empty to create in current project")
	@ChoiceProvider("getProjectChoices")
	public String getProjectPath() {
		return projectPath;
	}

	public void setProjectPath(String projectPath) {
		this.projectPath = projectPath;
	}
	
	@SuppressWarnings("unused")
	private static List<String> getProjectChoices() {
		ProjectService projectService = OneDev.getInstance(ProjectService.class);
		Project project = ((ProjectPage) WicketUtils.getPage()).getProject();

		Collection<Project> projects = SecurityUtils.getAuthorizedProjects(new AccessProject());
		projects.remove(project);

		ProjectCache cache = projectService.cloneCache();

		List<String> choices = projects.stream().map(it->cache.get(it.getId()).getPath()).collect(Collectors.toList());
		Collections.sort(choices);

		return choices;
	}

	@Editable(order=910, description="Specify a secret to be used as access token to create issue in " +
			"above project if it is not publicly accessible")
	@ChoiceProvider("getAccessTokenSecretChoices")
	@DependsOn(property="projectPath")
	@Nullable
	public String getAccessTokenSecret() {
		return accessTokenSecret;
	}

	public void setAccessTokenSecret(String accessTokenSecret) {
		this.accessTokenSecret = accessTokenSecret;
	}

	@SuppressWarnings("unused")
	private static List<String> getAccessTokenSecretChoices() {
		return Project.get().getHierarchyJobSecrets()
				.stream().map(it->it.getName()).collect(Collectors.toList());
	}
	
	@Editable(order=1000, name="Title", group="Issue Details", description="Specify title of the issue")
	@Interpolative(variableSuggester="suggestVariables")
	@NotEmpty
	public String getIssueTitle() {
		return issueTitle;
	}

	public void setIssueTitle(String issueTitle) {
		this.issueTitle = issueTitle;
	}
	
	@Editable(order=1050, name="Description", group="Issue Details", description="Optionally specify description of the issue")
	@Multiline
	@Interpolative(variableSuggester="suggestVariables")
	public String getIssueDescription() {
		return issueDescription;
	}

	public void setIssueDescription(String issueDescription) {
		this.issueDescription = issueDescription;
	}

	@Editable(order=1060, name="Confidential", group="Issue Details", description="Whether or not the issue should be confidential")
	public boolean isIssueConfidential() {
		return issueConfidential;
	}

	public void setIssueConfidential(boolean issueConfidential) {
		this.issueConfidential = issueConfidential;
	}

	@SuppressWarnings("unused")
	private static List<InputSuggestion> suggestVariables(String matchWith) {
		return BuildSpec.suggestVariables(matchWith, true, false, false);
	}
	
	@Editable(order=1100, group="Issue Details")
	@FieldNamesProvider("getFieldNames")
	@OmitName
	@Valid
	public List<FieldInstance> getIssueFields() {
		return issueFields;
	}

	public void setIssueFields(List<FieldInstance> issueFields) {
		this.issueFields = issueFields;
	}
	
	private static Collection<String> getFieldNames() {
		return OneDev.getInstance(SettingService.class).getIssueSetting().getFieldNames();
	}
	
	@Override
	public void execute(Build build) {
		OneDev.getInstance(TransactionService.class).run(() -> {
			Project project;
			if (getProjectPath() != null) {
				project = OneDev.getInstance(ProjectService.class).findByPath(getProjectPath());
				if (project == null) 
					throw new ExplicitException("Unable to find project: " + projectPath);
				Subject subject = JobAuthorizationContext.get().getSubject(getAccessTokenSecret());
				if (!SecurityUtils.canAccessProject(subject, project)) 
					throw new ExplicitException("Not authorized to create issue in project: " + getProjectPath());
			} else {
				project = build.getProject();
			}
			Issue issue = new Issue();
			issue.setProject(project);
			issue.setTitle(getIssueTitle());
			issue.setSubmitter(SecurityUtils.getUser());
			issue.setSubmitDate(new Date());
			SettingService settingService = OneDev.getInstance(SettingService.class);
			GlobalIssueSetting issueSetting = settingService.getIssueSetting();
			issue.setState(issueSetting.getInitialStateSpec().getName());
			
			issue.setDescription(getIssueDescription());
			issue.setConfidential(isIssueConfidential());
			for (FieldInstance instance: getIssueFields()) {
				Object fieldValue = issueSetting.getFieldSpec(instance.getName())
						.convertToObject(instance.getValueProvider().getValue());
				issue.setFieldValue(instance.getName(), fieldValue);
			}
			OneDev.getInstance(IssueService.class).open(issue);
		});
		
	}

	@Override
	public String getDescription() {
		return "Create issue";
	}

	@Override
	public void validateWith(BuildSpec buildSpec, Job job) {
		super.validateWith(buildSpec, job);
		
		GlobalIssueSetting issueSetting = OneDev.getInstance(SettingService.class).getIssueSetting();
		try {
			FieldUtils.validateFields(issueSetting.getFieldSpecMap(getFieldNames()), issueFields);
		} catch (ValidationException e) {
			throw new ValidationException("Error validating issue fields: " + e.getMessage());
		}
		
	}

}
