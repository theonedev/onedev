package io.onedev.server.buildspec.step.command.interpreter;

import java.util.List;

import javax.validation.constraints.Size;

import io.onedev.k8shelper.BorneShellExecutable;
import io.onedev.k8shelper.CommandExecutable;
import io.onedev.server.util.validation.annotation.Code;
import io.onedev.server.web.editable.annotation.Editable;
import io.onedev.server.web.editable.annotation.Interpolative;

@Editable(order=200, name="Borne Shell")
public class BorneShellInterpreter extends Interpreter {

	private static final long serialVersionUID = 1L;
	
	@Editable(order=110, description="Specify borne shell commands to execute "
			+ "under the <a href='$docRoot/pages/concepts.md#job-workspace' target='_blank'>job workspace</a>")
	@Interpolative
	@Code(language=Code.SHELL, variableProvider="suggestVariables")
	@Size(min=1, message="may not be empty")
	public List<String> getCommands() {
		return super.getCommands();
	}

	public void setCommands(List<String> commands) {
		super.setCommands(commands);
	}

	@Override
	public CommandExecutable getExecutable(String image, boolean useTTY) {
		return new BorneShellExecutable(image, getCommands(), useTTY);
	}

}
