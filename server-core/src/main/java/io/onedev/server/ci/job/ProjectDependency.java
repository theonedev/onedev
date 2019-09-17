package io.onedev.server.ci.job;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.NotEmpty;

import edu.emory.mathcs.backport.java.util.Collections;
import io.onedev.commons.codeassist.InputSuggestion;
import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.BuildManager;
import io.onedev.server.entitymanager.ProjectManager;
import io.onedev.server.model.Build;
import io.onedev.server.model.Project;
import io.onedev.server.util.OneContext;
import io.onedev.server.util.SecurityUtils;
import io.onedev.server.web.editable.annotation.BuildChoice;
import io.onedev.server.web.editable.annotation.ChoiceProvider;
import io.onedev.server.web.editable.annotation.Editable;
import io.onedev.server.web.editable.annotation.Patterns;
import io.onedev.server.web.page.project.ProjectPage;
import io.onedev.server.web.util.SuggestionUtils;
import io.onedev.server.web.util.WicketUtils;

@Editable
public class ProjectDependency implements Serializable {

	private static final long serialVersionUID = 1L;

	private Authentication authentication;
	
	private String projectName;

	private Long buildNumber;
	
	private String artifacts = "**";
	
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
	
	@Editable(order=300, name="Build", description="Specify build to retrieve artifacts from")
	@BuildChoice("getEditingProject")
	@NotNull
	public Long getBuildNumber() {
		return buildNumber;
	}

	public void setBuildNumber(Long buildNumber) {
		this.buildNumber = buildNumber;
	}
	
	@Nullable
	private static Project getEditingProject() {
		String projectName = (String) OneContext.get().getEditContext().getInputValue("projectName");
		if (projectName != null) {
			Project project = OneDev.getInstance(ProjectManager.class).find(projectName);
			if (SecurityUtils.canReadCode(project.getFacade()))
				return project;
		}
		return null;
	}

	@Editable(order=400, name="Artifacts to Retrieve", description="Specify artifacts to retrieve "
			+ "into job workspace")
	@Patterns("suggestArtifacts")
	@NotEmpty
	public String getArtifacts() {
		return artifacts;
	}

	public void setArtifacts(String artifacts) {
		this.artifacts = artifacts;
	}
	
	@SuppressWarnings("unused")
	private static List<InputSuggestion> suggestArtifacts(String matchWith) {
		Project project = getEditingProject();
		Long buildNumber = (Long) OneContext.get().getEditContext().getInputValue("buildNumber");
		if (project != null && buildNumber != null) {
			Build build = OneDev.getInstance(BuildManager.class).find(project, buildNumber);
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
