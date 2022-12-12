package io.onedev.server.buildspec.step.commandinterpreter;

import java.util.List;

import javax.validation.constraints.Size;

import io.onedev.k8shelper.CommandFacade;
import io.onedev.server.util.validation.annotation.Code;
import io.onedev.server.web.editable.annotation.Editable;
import io.onedev.server.web.editable.annotation.Interpolative;

@Editable(order=100, name="Default (Shell on Linux, Batch on Windows)")
public class DefaultInterpreter extends Interpreter {

	private static final long serialVersionUID = 1L;

	@Editable(order=110, description="Specify shell commands (on Linux/Unix) or batch commands (on Windows) to execute "
			+ "under the <a href='https://docs.onedev.io/concepts#job-workspace' target='_blank'>job workspace</a>")
	@Interpolative
	@Code(language=Code.SHELL, variableProvider="suggestVariables")
	@Size(min=1, message="may not be empty")
	@Override
	public List<String> getCommands() {
		return super.getCommands();
	}

	@Override
	public void setCommands(List<String> commands) {
		super.setCommands(commands);
	}

	@Override
	public CommandFacade getExecutable(String image, boolean useTTY) {
		return new CommandFacade(image, getCommands(), useTTY);
	}
	
}
