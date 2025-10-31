package io.onedev.server.buildspec.job.action;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.Nullable;
import javax.validation.ConstraintValidatorContext;
import javax.validation.constraints.NotEmpty;
import javax.ws.rs.NotAcceptableException;

import org.apache.shiro.authz.UnauthorizedException;
import org.apache.shiro.util.ThreadContext;

import io.onedev.commons.codeassist.InputSuggestion;
import io.onedev.server.OneDev;
import io.onedev.server.annotation.ChoiceProvider;
import io.onedev.server.annotation.ClassValidating;
import io.onedev.server.annotation.Editable;
import io.onedev.server.annotation.Interpolative;
import io.onedev.server.buildspec.BuildSpec;
import io.onedev.server.git.GitUtils;
import io.onedev.server.job.JobService;
import io.onedev.server.model.Build;
import io.onedev.server.model.Project;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.security.permission.AccessProject;
import io.onedev.server.service.AccessTokenService;
import io.onedev.server.service.BuildService;
import io.onedev.server.service.ProjectService;
import io.onedev.server.util.EditContext;
import io.onedev.server.util.facade.ProjectCache;
import io.onedev.server.validation.Validatable;
import io.onedev.server.web.page.project.ProjectPage;
import io.onedev.server.web.util.SuggestionUtils;
import io.onedev.server.web.util.WicketUtils;

@Editable(name="Run job in another project", order=150)
@ClassValidating
public class RunProjectJobAction extends PostBuildAction implements Validatable {

	private static final long serialVersionUID = 1L;

	private String projectPath;

	private String branch;

	private String tag;

	private String jobName;

	private List<JobParam> jobParams = new ArrayList<>();

	private String accessTokenSecret;
		
	@Editable(order=100, name="Project", description="Specify project to run job in")
	@ChoiceProvider("getProjectChoices")
	@NotEmpty
	public String getProjectPath() {
		return projectPath;
	}

	public void setProjectPath(String projectPath) {
		this.projectPath = projectPath;
	}

	@SuppressWarnings("unused")
	private static List<String> getProjectChoices() {
		List<String> choices = new ArrayList<>();
		Project currentProject = ((ProjectPage)WicketUtils.getPage()).getProject();
		
		ProjectCache cache = getProjectService().cloneCache();
		for (Project project: SecurityUtils.getAuthorizedProjects(new AccessProject())) {
			if (!project.equals(currentProject))
				choices.add(cache.get(project.getId()).getPath());
		}
		
		Collections.sort(choices);
		
		return choices;
	}

	@Editable(order=200, description="Specify branch to run the job against. Either branch or tag can be specified, but not both. Default branch will be used if both not specified")
	@Interpolative(variableSuggester="suggestVariables", literalSuggester="suggestBranches")
	public String getBranch() {
		return branch;
	}

	public void setBranch(String branch) {
		this.branch = branch;
	}

	@Editable(order=200, description="Specify tag to run the job against. Either branch or tag can be specified, but not both. Default branch will be used if both not specified")
	@Interpolative(variableSuggester="suggestVariables", literalSuggester="suggestTags")
	public String getTag() {
		return tag;
	}

	public void setTag(String tag) {
		this.tag = tag;
	}

	@SuppressWarnings("unused")
	private static List<InputSuggestion> suggestVariables(String matchWith) {
		return BuildSpec.suggestVariables(matchWith, false, false, false);
	}
	
	@SuppressWarnings("unused")
	private static List<InputSuggestion> suggestBranches(String matchWith) {
		Project project = getInputProject();
		if (project != null)
			return SuggestionUtils.suggestBranches(project, matchWith);
		else
			return new ArrayList<>();
	}

	@SuppressWarnings("unused")
	private static List<InputSuggestion> suggestTags(String matchWith) {
		Project project = getInputProject();
		if (project != null)
			return SuggestionUtils.suggestTags(project, matchWith);
		else
			return new ArrayList<>();
	}

	@Editable(order=300, name="Job")
	@ChoiceProvider("getJobChoices")
	@NotEmpty
	public String getJobName() {
		return jobName;
	}

	public void setJobName(String jobName) {
		this.jobName = jobName;
	}

	@Nullable
	private static Project getInputProject() {
		String projectPath = (String) EditContext.get().getInputValue("projectPath");
		if (projectPath != null) {
			Project project = getProjectService().findByPath(projectPath);
			if (project != null && SecurityUtils.canReadCode(project))
				return project;
		}
		return null;
	}

	@SuppressWarnings("unused")
	private static List<String> getJobChoices() {
		Project project = getInputProject();
		List<String> jobNames = new ArrayList<>();
		if (project != null) {
			jobNames.addAll(getBuildService().getAccessibleJobNames(SecurityUtils.getSubject(), project));
			Collections.sort(jobNames);
		}
		return jobNames;
	}
	
	@Editable(order=400, name="Job Parameters")
	public List<JobParam> getJobParams() {
		return jobParams;
	}

	public void setJobParams(List<JobParam> jobParams) {
		this.jobParams = jobParams;
	}

	@Editable(order=500, placeholder="Access Anonymously", description="Specify a secret to be used as "
			+ "access token to trigger job in above project")
	@ChoiceProvider("getAccessTokenSecretChoices")
	@NotEmpty
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

	@Override
	public void execute(Build build) {
		Project project = getProjectService().findByPath(projectPath);
		if (project == null)
			throw new NotAcceptableException("Project not found: " + projectPath);

		String secretValue = build.getJobAuthorizationContext().getSecretValue(accessTokenSecret);
		var accessToken = getAccessTokenService().findByValue(secretValue);
		if (accessToken == null)
			throw new NotAcceptableException("Invalid access token");
		
		var subject = accessToken.asSubject();
		if (!SecurityUtils.canRunJob(subject, project, jobName))		
			throw new UnauthorizedException();

		var user = SecurityUtils.getUser(subject);
		ThreadContext.bind(subject);
		try {
			String refName;
			if (branch != null) {
				refName = GitUtils.branch2ref(branch);
			} else if (tag != null) {
				refName = GitUtils.tag2ref(tag);
			} else {
				var defaultBranch = project.getDefaultBranch();
				if (defaultBranch == null)
 					throw new NotAcceptableException("No default branch in project: " + project.getPath());
				refName = GitUtils.branch2ref(defaultBranch);
			}
			var commit = project.getRevCommit(refName, false);
			if (commit == null)
				throw new NotAcceptableException("Ref not found (project: " + project.getPath() + ", ref: " + refName + ")");
						
			Map<String, List<String>> jobParamMap = new HashMap<>();
			for (var jobParam: jobParams) {
				jobParamMap.computeIfAbsent(jobParam.getName(), k -> new ArrayList<>()).add(jobParam.getValue());
			}
			
			getJobService().submit(user, project, commit.copy(), jobName, jobParamMap, refName, null, null, 
					"Triggered via post build action of job '" + build.getJobName() + "' in project '" + build.getProject().getPath() + "'");
		} finally {
			ThreadContext.unbindSubject();
		}		
	}

	@Override
	public boolean isValid(ConstraintValidatorContext context) {
		if (branch != null && tag != null) {
			var errorMessage = "Either branch or tag can be specified, but not both";
			context.buildConstraintViolationWithTemplate(errorMessage)
					.addPropertyNode("branch").addConstraintViolation();
			context.buildConstraintViolationWithTemplate(errorMessage)
					.addPropertyNode("tag").addConstraintViolation();
			return false;
		} else {
			return true;
		}
	}

	@Override
	public String getDescription() {
		return "Run job '" + jobName + "' in project '" + projectPath + "'";
	}

	private static ProjectService getProjectService() {
		return OneDev.getInstance(ProjectService.class);
	}

	private static AccessTokenService getAccessTokenService() {
		return OneDev.getInstance(AccessTokenService.class);
	}

	private static JobService getJobService() {
		return OneDev.getInstance(JobService.class);
	}

	private static BuildService getBuildService() {
		return OneDev.getInstance(BuildService.class);
	}

	@Editable
	public static class JobParam implements Serializable {
	
		private static final long serialVersionUID = 1L;
	
		private String name;
	
		private String value;
	
		@Editable(order=100)
		@Interpolative(variableSuggester="suggestVariables")
		@NotEmpty
		public String getName() {
			return name;
		}
	
		public void setName(String name) {
			this.name = name;
		}
	
		@Editable(order=200)
		@Interpolative(variableSuggester="suggestVariables")
		@NotEmpty
		public String getValue() {
			return value;
		}
	
		public void setValue(String value) {
			this.value = value;
		}
	
		@SuppressWarnings("unused")
		private static List<InputSuggestion> suggestVariables(String matchWith) {
			return BuildSpec.suggestVariables(matchWith, false, false, false);
		}
		
	}

}
