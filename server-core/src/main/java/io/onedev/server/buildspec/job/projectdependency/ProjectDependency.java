package io.onedev.server.buildspec.job.projectdependency;

import io.onedev.commons.codeassist.InputSuggestion;
import io.onedev.server.OneDev;
import io.onedev.server.annotation.ChoiceProvider;
import io.onedev.server.annotation.Editable;
import io.onedev.server.annotation.Interpolative;
import io.onedev.server.annotation.Path;
import io.onedev.server.annotation.Patterns;
import io.onedev.server.buildspec.BuildSpec;
import io.onedev.server.model.support.build.JobSecret;
import io.onedev.server.service.ProjectService;
import io.onedev.server.model.Project;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.security.permission.AccessProject;
import io.onedev.server.util.EditContext;
import io.onedev.server.util.facade.ProjectCache;
import io.onedev.server.web.page.project.ProjectPage;
import io.onedev.server.web.util.WicketUtils;

import org.jspecify.annotations.Nullable;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Editable
public class ProjectDependency implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	private String projectPath;

	private BuildProvider buildProvider = new LatestBuild();

	private boolean requireSuccessful = true;
	
	private String artifacts;
	
	private String destinationPath;
	
	private String accessTokenSecret;
	
	// change Named("projectPath") also if change name of this property 
	@Editable(order=200, name="Project", description="Specify project to retrieve artifacts from")
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
		
		ProjectService projectService = OneDev.getInstance(ProjectService.class);
		ProjectCache cache = projectService.cloneCache();
		for (Project project: SecurityUtils.getAuthorizedProjects(new AccessProject())) {
			if (!project.equals(currentProject))
				choices.add(cache.get(project.getId()).getPath());
		}
		
		Collections.sort(choices);
		
		return choices;
	}

	@Editable(order=300, name="Build")
	@NotNull
	public BuildProvider getBuildProvider() {
		return buildProvider;
	}

	public void setBuildProvider(BuildProvider buildProvider) {
		this.buildProvider = buildProvider;
	}

	@Editable(order=350, description="Whether or not to require this dependency to be successful")
	public boolean isRequireSuccessful() {
		return requireSuccessful;
	}

	public void setRequireSuccessful(boolean requireSuccessful) {
		this.requireSuccessful = requireSuccessful;
	}

	@Nullable
	static Project getInputProject(EditContext editContext) {
		String projectPath = (String) editContext.getInputValue("projectPath");
		if (projectPath != null) {
			Project project = OneDev.getInstance(ProjectService.class).findByPath(projectPath);
			if (project != null && SecurityUtils.canReadCode(project))
				return project;
		}
		return null;
	}
	
	@Editable(order=400, name="Artifacts to Retrieve", placeholder="Do not retrieve", description=""
			+ "Optionally specify artifacts to retrieve from the dependency into "
			+ "<a href='${docsite}/concepts#job-workdir'>job working directory</a>. "
			+ "Only published artifacts (via artifact publish step) can be retrieved. Leave empty to not "
			+ "retrieve any artifacts")
	@Interpolative(variableSuggester="suggestVariables")
	@Patterns(path=true)
	@Nullable
	public String getArtifacts() {
		return artifacts;
	}

	public void setArtifacts(String artifacts) {
		this.artifacts = artifacts;
	}
	
	@Editable(order=500, placeholder="Job workdir", description= """
            Optionally specify a path relative to <a href='${docsite}/concepts#job-workdir'>job working directory</a> \
            to put retrieved artifacts. Leave empty to use job working directory itself""")
	@Interpolative(variableSuggester="suggestVariables")
	@Path(Path.Type.RELATIVE)
	public String getDestinationPath() {
		return destinationPath;
	}

	public void setDestinationPath(String destinationPath) {
		this.destinationPath = destinationPath;
	}
	
	@SuppressWarnings("unused")
	private static List<InputSuggestion> suggestVariables(String matchWith) {
		return BuildSpec.suggestVariables(matchWith, false, false, false);
	}
	
	@Editable(order=500, placeholder="Access Anonymously", description="Specify a secret to be used as "
			+ "access token to retrieve artifacts from above project. If not specified, project "
			+ "artifacts will be accessed anonymously")
	@ChoiceProvider("getAccessTokenSecretChoices")
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
				.stream().map(JobSecret::getName).collect(Collectors.toList());
	}
	
}
