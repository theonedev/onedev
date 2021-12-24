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
import io.onedev.server.util.EditContext;
import io.onedev.server.util.validation.annotation.Code;
import io.onedev.server.web.editable.annotation.Editable;
import io.onedev.server.web.editable.annotation.Interpolative;
import io.onedev.server.web.editable.annotation.ShowCondition;

@Editable(order=10, name="Execute Shell/Batch Commands")
public class CommandStep extends Step {

	private static final long serialVersionUID = 1L;

	private boolean runInContainer = true;
	
	private String image;
	
	private List<String> commands = new ArrayList<>();
	
	private boolean useTTY;
	
	@Editable(order=50, description="Whether or not to run this step inside container")
	public boolean isRunInContainer() {
		return runInContainer;
	}

	public void setRunInContainer(boolean runInContainer) {
		this.runInContainer = runInContainer;
	}

	@SuppressWarnings("unused")
	private static boolean isRunInContainerEnabled() {
		return (boolean) EditContext.get().getInputValue("runInContainer");
	}
	
	@Editable(order=100, description="Specify container image to execute commands inside")
	@ShowCondition("isRunInContainerEnabled")
	@Interpolative(variableSuggester="suggestVariables")
	@NotEmpty
	public String getImage() {
		return image;
	}

	public void setImage(String image) {
		this.image = image;
	}

	@Editable(order=110, description="Specify shell (on Linux/Unix) or batch (on Windows) commands to execute "
			+ "under the <a href='$docRoot/pages/concepts.md#job-workspace' target='_blank'>job workspace</a>. Depending on the "
			+ "job executor being used, this may be executed either inside or outside container")
	@Interpolative
	@Code(language=Code.SHELL, variableProvider="suggestVariables")
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

	static List<InputSuggestion> suggestVariables(String matchWith) {
		return BuildSpec.suggestVariables(matchWith, false, false);
	}
	
	@Override
	public Executable getExecutable(Build build, String jobToken, ParamCombination paramCombination) {
		return new CommandExecutable(runInContainer?getImage():null, getCommands(), isUseTTY());
	}
	
}
