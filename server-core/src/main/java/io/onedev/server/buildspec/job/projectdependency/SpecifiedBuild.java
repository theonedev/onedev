package io.onedev.server.buildspec.job.projectdependency;

import java.util.ArrayList;
import java.util.List;

import javax.validation.constraints.NotEmpty;

import io.onedev.commons.codeassist.InputSuggestion;
import io.onedev.server.OneDev;
import io.onedev.server.buildspec.BuildSpec;
import io.onedev.server.entitymanager.BuildManager;
import io.onedev.server.model.Build;
import io.onedev.server.model.Project;
import io.onedev.server.util.EditContext;
import io.onedev.server.web.behavior.inputassist.InputAssistBehavior;
import io.onedev.server.annotation.Editable;
import io.onedev.server.annotation.Interpolative;
import io.onedev.server.annotation.OmitName;
import io.onedev.server.web.util.SuggestionUtils;

@Editable(order=200, name="Specify by Build Number")
public class SpecifiedBuild implements BuildProvider {

	private static final long serialVersionUID = 1L;
	
	private String buildNumber;
	
	@Editable(order=300)
	@OmitName
	@Interpolative(variableSuggester="suggestVariables", literalSuggester="suggestBuilds")
	@NotEmpty
	public String getBuildNumber() {
		return buildNumber;
	}

	public void setBuildNumber(String buildNumber) {
		this.buildNumber = buildNumber;
	}

	@SuppressWarnings("unused")
	private static List<InputSuggestion> suggestVariables(String matchWith) {
		return BuildSpec.suggestVariables(matchWith, false, false, false);
	}
	
	@SuppressWarnings("unused")
	private static List<InputSuggestion> suggestBuilds(String matchWith) {
		Project project = ProjectDependency.getInputProject(EditContext.get(1));
		if (project != null)
			return SuggestionUtils.suggestBuilds(project, matchWith, InputAssistBehavior.MAX_SUGGESTIONS);
		else
			return new ArrayList<>();
	}

	@Override
	public Build getBuild(Project project) {
		Long buildNumber;
		if (this.buildNumber.startsWith("#"))
			buildNumber = Long.parseLong(this.buildNumber.substring(1));
		else
			buildNumber = Long.parseLong(this.buildNumber);
		
		return OneDev.getInstance(BuildManager.class).find(project, buildNumber);
	}

	@Override
	public String getDescription() {
		return buildNumber;
	}

}
