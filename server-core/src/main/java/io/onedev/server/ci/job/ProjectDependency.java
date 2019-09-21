package io.onedev.server.ci.job;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import org.hibernate.validator.constraints.NotEmpty;

import edu.emory.mathcs.backport.java.util.Collections;
import io.onedev.commons.codeassist.InputSuggestion;
import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.BuildManager;
import io.onedev.server.entitymanager.ProjectManager;
import io.onedev.server.model.Build;
import io.onedev.server.model.Project;
import io.onedev.server.util.EditContext;
import io.onedev.server.util.SecurityUtils;
import io.onedev.server.web.editable.annotation.ChoiceProvider;
import io.onedev.server.web.editable.annotation.Editable;
import io.onedev.server.web.editable.annotation.Interpolative;
import io.onedev.server.web.editable.annotation.Patterns;
import io.onedev.server.web.page.project.ProjectPage;
import io.onedev.server.web.util.SuggestionUtils;
import io.onedev.server.web.util.WicketUtils;

@Editable
public class ProjectDependency implements Serializable {

	private static final long serialVersionUID = 1L;

	private Authentication authentication;
	
	private String projectName;

	private String buildNumber;
	
	private String artifacts = "**";
	
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
		for (Project each: OneDev.getInstance(ProjectManager.class).query()) {
			if (!each.equals(project) && SecurityUtils.canReadCode(each.getFacade()))
				choices.add(each.getName());
		}
		
		Collections.sort(choices);
		
		return choices;
	}
	
	@Editable(order=300, name="Build", description="Specify build to retrieve artifacts from. "
			+ "<b>Note:</b> Type '@' to start inserting variable")
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
			if (project != null && SecurityUtils.canReadCode(project.getFacade()))
				return project;
		}
		return null;
	}
	
	@SuppressWarnings("unused")
	private static List<InputSuggestion> suggestBuilds(String matchWith) {
		Project project = getInputProject();
		if (project != null)
			return SuggestionUtils.suggestBuilds(project, matchWith);
		else
			return new ArrayList<>();
	}

	@Editable(order=400, name="Artifacts to Retrieve", description="Specify artifacts to retrieve "
			+ "into job workspace. <b>Note:</b> Type '@' to start inserting variable")
	@Interpolative(variableSuggester="suggestVariables")
	@Patterns(suggester="suggestArtifacts")
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
		if (project != null && io.onedev.server.util.interpolative.Interpolative.fromString(buildNumber).getSegments().isEmpty()) {
			Build build = OneDev.getInstance(BuildManager.class).find(project, Long.parseLong(buildNumber));
			if (build != null)
				return SuggestionUtils.suggestArtifacts(build, matchWith);
		}
		return new ArrayList<>();
	}

	@Editable(order=500, description="Optionally authenticate to specified project. If not specified, "
			+ "project artifacts will be accessed anonymously")
	@Nullable
	public Authentication getAuthentication() {
		return authentication;
	}

	public void setAuthentication(Authentication authentication) {
		this.authentication = authentication;
	}

}
