package io.onedev.server.buildspec.step;

import java.util.ArrayList;
import java.util.List;

import javax.validation.constraints.Size;

import org.hibernate.validator.constraints.NotEmpty;

import io.onedev.commons.codeassist.InputSuggestion;
import io.onedev.k8shelper.CommandExecutable;
import io.onedev.k8shelper.Executable;
import io.onedev.server.buildspec.BuildSpec;
import io.onedev.server.buildspec.param.ParamCombination;
import io.onedev.server.model.Build;
import io.onedev.server.util.validation.annotation.Code;
import io.onedev.server.web.editable.annotation.Editable;
import io.onedev.server.web.editable.annotation.Interpolative;

@Editable(order=10, name="Execute Shell/Batch Commands")
public class CommandStep extends Step {

	private static final long serialVersionUID = 1L;

	private String image;
	
	private List<String> commands = new ArrayList<>();
	
	private boolean useTTY;
	
	@Editable(order=100, description="Specify docker image to execute commands inside. "
			+ "<span class='text-warning'>This property will be ignored if the job is executed by a shell executor</span>")
	@Interpolative(variableSuggester="suggestVariables")
	@NotEmpty
	public String getImage() {
		return image;
	}

	public void setImage(String image) {
		this.image = image;
	}

	@Editable(order=110, description="Specify content of Linux shell script or Windows command batch to execute "
			+ "under the <a href='$docRoot/pages/concepts.md#job-workspace' target='_blank'>job workspace</a>. Depending on the "
			+ "job executor being used, this may be executed either inside or outside container")
	@Interpolative
	@Code(language=Code.SHELL, variableProvider="suggestCommandVariables")
	@Size(min=1, message="may not be empty")
	public List<String> getCommands() {
		return commands;
	}

	public void setCommands(List<String> commands) {
		this.commands = commands;
	}
	
	@Editable(order=120, name="Enable TTY Mode", description="Many commands print outputs with ANSI colors in "
			+ "TTY mode to help identifying problems easily. However some commands running in this mode may "
			+ "wait for user input to cause build hanging. This can normally be fixed by adding extra options "
			+ "to the command. <span class='text-warning'>This option will be ignored when the job is executed "
			+ "via a shell executor</span>")
	public boolean isUseTTY() {
		return useTTY;
	}

	public void setUseTTY(boolean useTTY) {
		this.useTTY = useTTY;
	}

	@SuppressWarnings("unused")
	private static List<InputSuggestion> suggestVariables(String matchWith) {
		return BuildSpec.suggestVariables(matchWith, false, false);
	}
	
	@SuppressWarnings("unused")
	private static List<InputSuggestion> suggestCommandVariables(String matchWith) {
		return BuildSpec.suggestVariables(matchWith, true, true);
	}
	
	@Override
	public Executable getExecutable(Build build, String jobToken, ParamCombination paramCombination) {
		return new CommandExecutable(getImage(), getCommands(), isUseTTY());
	}
	
}
