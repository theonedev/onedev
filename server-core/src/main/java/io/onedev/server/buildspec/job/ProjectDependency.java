package io.onedev.server.buildspec.job;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import org.hibernate.validator.constraints.NotEmpty;

import edu.emory.mathcs.backport.java.util.Collections;
import io.onedev.commons.codeassist.InputSuggestion;
import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.BuildManager;
import io.onedev.server.entitymanager.ProjectManager;
import io.onedev.server.model.Build;
import io.onedev.server.model.Project;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.security.permission.AccessProject;
import io.onedev.server.util.EditContext;
import io.onedev.server.util.interpolative.Interpolative.Segment;
import io.onedev.server.web.behavior.inputassist.InputAssistBehavior;
import io.onedev.server.web.editable.annotation.ChoiceProvider;
import io.onedev.server.web.editable.annotation.Editable;
import io.onedev.server.web.editable.annotation.Interpolative;
import io.onedev.server.web.editable.annotation.NameOfEmptyValue;
import io.onedev.server.web.editable.annotation.Patterns;
import io.onedev.server.web.page.project.ProjectPage;
import io.onedev.server.web.util.SuggestionUtils;
import io.onedev.server.web.util.WicketUtils;

@Editable
public class ProjectDependency implements Serializable {

	private static final long serialVersionUID = 1L;

	private String projectName;

	private String buildNumber;
	
	private String artifacts = "**";
	
	private String accessTokenSecret;
	
	// change Named("projectName") also if change name of this property 
	@Editable(order=200, name="Project", description="Specify project to retrieve artifacts from")
	@ChoiceProvider("getProjectChoices")
	@NotEmpty
	public String getProjectName() {
		return projectName;
	}

	public void setProjectName(String projectName) {
		this.projectName = projectName;
	}

	@SuppressWarnings("unused")
	private static List<String> getProjectChoices() {
		List<String> choices = new ArrayList<>();
		Project project = ((ProjectPage)WicketUtils.getPage()).getProject();
		for (Project each: OneDev.getInstance(ProjectManager.class).getPermittedProjects(new AccessProject())) {
			if (!each.equals(project))
				choices.add(each.getName());
		}
		
		Collections.sort(choices);
		
		return choices;
	}
	
	@Editable(order=300, name="Build", description="Specify build to retrieve artifacts from")
	@Interpolative(variableSuggester="suggestVariables", literalSuggester="suggestBuilds")
	@NotEmpty
	public String getBuildNumber() {
		return buildNumber;
	}

	public void setBuildNumber(String buildNumber) {
		this.buildNumber = buildNumber;
	}

	@Nullable
	private static Project getInputProject() {
		String projectName = (String) EditContext.get().getInputValue("projectName");
		if (projectName != null) {
			Project project = OneDev.getInstance(ProjectManager.class).find(projectName);
			if (project != null && SecurityUtils.canReadCode(project))
				return project;
		}
		return null;
	}
	
	@SuppressWarnings("unused")
	private static List<InputSuggestion> suggestBuilds(String matchWith) {
		Project project = getInputProject();
		if (project != null)
			return SuggestionUtils.suggestBuilds(project, matchWith, InputAssistBehavior.MAX_SUGGESTIONS);
		else
			return new ArrayList<>();
	}

	@Editable(order=400, name="Artifacts to Retrieve", description="Specify artifacts to retrieve into <a href='$docRoot/pages/concepts.md#job-workspace' target='_blank'>job workspace</a>")
	@Interpolative(variableSuggester="suggestVariables")
	@Patterns(suggester="suggestArtifacts", path=true)
	@NotEmpty
	public String getArtifacts() {
		return artifacts;
	}

	public void setArtifacts(String artifacts) {
		this.artifacts = artifacts;
	}
	
	@SuppressWarnings("unused")
	private static List<InputSuggestion> suggestVariables(String matchWith) {
		return Job.suggestVariables(matchWith);
	}
	
	@SuppressWarnings("unused")
	private static List<InputSuggestion> suggestArtifacts(String matchWith) {
		Project project = getInputProject();
		String buildNumber = (String) EditContext.get().getInputValue("buildNumber");
		if (project != null && io.onedev.server.util.interpolative.Interpolative.parse(buildNumber).getSegments(Segment.Type.VARIABLE).isEmpty()) {
			if (buildNumber.startsWith("#"))
				buildNumber = buildNumber.substring(1);
			Build build = OneDev.getInstance(BuildManager.class).find(project, Long.parseLong(buildNumber));
			if (build != null)
				return SuggestionUtils.suggestArtifacts(build, matchWith);
		}
		return new ArrayList<>();
	}

	@Editable(order=500, description="Specify a secret to be used as access token to retrieve artifacts "
			+ "from specified project. If not specified, project artifacts will be accessed anonymously")
	@ChoiceProvider("getAccessTokenSecretChoices")
	@Nullable
	@NameOfEmptyValue("Access Anonymously")
	public String getAccessTokenSecret() {
		return accessTokenSecret;
	}

	public void setAccessTokenSecret(String accessTokenSecret) {
		this.accessTokenSecret = accessTokenSecret;
	}
	
	@SuppressWarnings("unused")
	private static List<String> getAccessTokenSecretChoices() {
		return Project.get().getBuildSetting().getJobSecrets()
				.stream().map(it->it.getName()).collect(Collectors.toList());
	}
	
}
