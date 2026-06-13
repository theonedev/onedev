package io.onedev.server.buildspec.step.commandinterpreter;

import javax.validation.constraints.NotEmpty;

import io.onedev.k8shelper.InterpreterFacade;
import io.onedev.k8shelper.PosixInterpreterFacade;
import io.onedev.server.annotation.Code;
import io.onedev.server.annotation.Editable;
import io.onedev.server.annotation.Interpolative;

@Editable(order=100, name="POSIX Compatible Shell")
public class PosixInterpreter extends Interpreter {

	private static final long serialVersionUID = 1L;

	private String shell = "sh";

	@Editable(order=100, name="Shell Executable", description="Specify POSIX shell executable to be used")
	@Interpolative(variableSuggester = "suggestVariables")
	@NotEmpty
	public String getShell() {
		return shell;
	}

	public void setShell(String shell) {
		this.shell = shell;
	}

	@Editable(order=110, description="Specify shell commands to execute "
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
		return new PosixInterpreterFacade(getCommands(), getShell());
	}

}
