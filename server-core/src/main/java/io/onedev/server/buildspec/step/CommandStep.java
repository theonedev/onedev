package io.onedev.server.buildspec.step;

import java.util.ArrayList;
import java.util.List;

import javax.validation.constraints.Size;

import org.eclipse.jgit.lib.ObjectId;
import org.hibernate.validator.constraints.NotEmpty;

import io.onedev.commons.codeassist.InputSuggestion;
import io.onedev.k8shelper.CommandExecutable;
import io.onedev.k8shelper.Executable;
import io.onedev.server.buildspec.BuildSpec;
import io.onedev.server.buildspec.param.ParamCombination;
import io.onedev.server.buildspec.param.spec.ParamSpec;
import io.onedev.server.model.Build;
import io.onedev.server.model.Project;
import io.onedev.server.web.editable.annotation.Code;
import io.onedev.server.web.editable.annotation.Editable;
import io.onedev.server.web.editable.annotation.Interpolative;
import io.onedev.server.web.page.project.blob.ProjectBlobPage;
import io.onedev.server.web.util.SuggestionUtils;
import io.onedev.server.web.util.WicketUtils;

@Editable(order=100, name="Execute Shell/Batch Commands")
public class CommandStep extends Step {

	private static final long serialVersionUID = 1L;

	private String image;
	
	private List<String> commands = new ArrayList<>();
	
	@Editable(order=100, description="Specify docker image to execute commands inside")
	@Interpolative(variableSuggester="suggestVariables")
	@NotEmpty
	public String getImage() {
		return image;
	}

	public void setImage(String image) {
		this.image = image;
	}

	@Editable(order=110, description="Specify content of Linux shell script or Windows command batch to execute "
			+ "under the repository root")
	@Interpolative
	@Code(language = Code.SHELL, variableProvider="getVariables")
	@Size(min=1, message="may not be empty")
	public List<String> getCommands() {
		return commands;
	}

	public void setCommands(List<String> commands) {
		this.commands = commands;
	}
	
	@SuppressWarnings("unused")
	private static List<InputSuggestion> suggestVariables(String matchWith) {
		return BuildSpec.suggestVariables(matchWith);
	}
	
	@SuppressWarnings("unused")
	private static List<String> getVariables() {
		List<String> variables = new ArrayList<>();
		ProjectBlobPage page = (ProjectBlobPage) WicketUtils.getPage();
		Project project = page.getProject();
		ObjectId commitId = page.getCommit();
		for (InputSuggestion suggestion: SuggestionUtils
				.suggestVariables(project, BuildSpec.get(), ParamSpec.list(), "")) {  
			variables.add(suggestion.getContent());
		}
		return variables;
	}

	@Override
	public Executable getExecutable(Build build, ParamCombination paramCombination) {
		return new CommandExecutable(getImage(), getCommands());
	}
	
}
