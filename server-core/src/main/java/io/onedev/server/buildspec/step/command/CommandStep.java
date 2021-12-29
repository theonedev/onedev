package io.onedev.server.buildspec.step.command;

import java.util.List;

import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.NotEmpty;

import io.onedev.commons.codeassist.InputSuggestion;
import io.onedev.k8shelper.Executable;
import io.onedev.server.buildspec.BuildSpec;
import io.onedev.server.buildspec.param.ParamCombination;
import io.onedev.server.buildspec.step.Step;
import io.onedev.server.buildspec.step.command.interpreter.DefaultInterpreter;
import io.onedev.server.buildspec.step.command.interpreter.Interpreter;
import io.onedev.server.model.Build;
import io.onedev.server.util.EditContext;
import io.onedev.server.web.editable.annotation.Editable;
import io.onedev.server.web.editable.annotation.Interpolative;
import io.onedev.server.web.editable.annotation.ShowCondition;

@Editable(order=100, name="Execute Commands")
public class CommandStep extends Step {

	private static final long serialVersionUID = 1L;

	private boolean runInContainer = true;
	
	private String image;
	
	private Interpreter interpreter = new DefaultInterpreter();
	
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

	@Editable(order=110)
	@NotNull
	public Interpreter getInterpreter() {
		return interpreter;
	}

	public void setInterpreter(Interpreter interpreter) {
		this.interpreter = interpreter;
	}

	@Editable(order=120, name="Enable TTY Mode", description="Many commands print outputs with ANSI colors in "
			+ "TTY mode to help identifying problems easily. However some commands running in this mode may "
			+ "wait for user input to cause build hanging. This can normally be fixed by adding extra options "
			+ "to the command")
	@ShowCondition("isRunInContainerEnabled")
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
		return interpreter.getExecutable(runInContainer?getImage():null, isUseTTY());
	}
	
}
