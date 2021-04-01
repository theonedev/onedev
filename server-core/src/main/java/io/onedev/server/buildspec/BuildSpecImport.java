package io.onedev.server.buildspec;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import org.hibernate.validator.constraints.NotEmpty;

import edu.emory.mathcs.backport.java.util.Collections;
import io.onedev.commons.codeassist.InputSuggestion;
import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.ProjectManager;
import io.onedev.server.model.Project;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.security.permission.AccessProject;
import io.onedev.server.util.EditContext;
import io.onedev.server.web.editable.annotation.ChoiceProvider;
import io.onedev.server.web.editable.annotation.Editable;
import io.onedev.server.web.editable.annotation.Interpolative;
import io.onedev.server.web.editable.annotation.NameOfEmptyValue;
import io.onedev.server.web.page.project.ProjectPage;
import io.onedev.server.web.util.SuggestionUtils;
import io.onedev.server.web.util.WicketUtils;

@Editable
public class BuildSpecImport implements Serializable {

	private static final long serialVersionUID = 1L;

	private String projectName;
	
	private String tag;

	private String accessTokenSecret;
	
	// change Named("projectName") also if change name of this property 
	@Editable(order=100, name="Project", description="Specify project to import build spec from")
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
	
	@Editable(order=200, description="Specify tag in above project to import build spec from")
	@Interpolative(variableSuggester="suggestTags", literalSuggester="suggestTags")
	@NotEmpty
	public String getTag() {
		return tag;
	}

	public void setTag(String tag) {
		this.tag = tag;
	}

	@SuppressWarnings("unused")
	private static List<InputSuggestion> suggestTags(String matchWith) {
		Project project = getInputProject();
		if (project != null)
			return SuggestionUtils.suggestTags(project, matchWith);
		else
			return new ArrayList<>();
	}

	@SuppressWarnings("unused")
	private static List<InputSuggestion> suggestVariables(String matchWith) {
		return BuildSpec.suggestVariables(matchWith);
	}

	@Editable(order=500, description="Specify a secret to be used as access token to import build spec "
			+ "from specified project. Leave empty if the project code can be read anonymously")
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
