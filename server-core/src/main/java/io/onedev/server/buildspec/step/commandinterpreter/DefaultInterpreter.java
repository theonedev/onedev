package io.onedev.server.buildspec.step.commandinterpreter;

import javax.validation.constraints.NotEmpty;

import io.onedev.k8shelper.DefaultInterpreterFacade;
import io.onedev.k8shelper.InterpreterFacade;
import io.onedev.server.annotation.Code;
import io.onedev.server.annotation.Editable;
import io.onedev.server.annotation.Interpolative;

@Editable(order=100, name="Default (Shell on Linux, Batch on Windows)")
public class DefaultInterpreter extends Interpreter {

	private static final long serialVersionUID = 1L;

	@Editable(order=110, description="Specify shell commands (on Linux/Unix) or batch commands (on Windows) to execute "
			+ "under the <a href='https://docs.onedev.io/concepts#job-workdir' target='_blank'>job working directory</a>")
	@Interpolative
	@Code(language=Code.SHELL, variableProvider="suggestVariables")
	@NotEmpty
	@Override
	public String getCommands() {
		return super.getCommands();
	}

	@Override
	public void setCommands(String commands) {
		super.setCommands(commands);
	}

	@Override
	public InterpreterFacade getFacade() {
		return new DefaultInterpreterFacade(getCommands());
	}
	
}
