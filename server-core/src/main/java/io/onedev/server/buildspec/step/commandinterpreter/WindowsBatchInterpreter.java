package io.onedev.server.buildspec.step.commandinterpreter;

import javax.validation.constraints.NotEmpty;

import io.onedev.k8shelper.InterpreterFacade;
import io.onedev.k8shelper.WindowsBatchInterpreterFacade;
import io.onedev.server.annotation.Code;
import io.onedev.server.annotation.Editable;
import io.onedev.server.annotation.Interpolative;

@Editable(order=200, name="Windows Batch")
public class WindowsBatchInterpreter extends Interpreter {

	private static final long serialVersionUID = 1L;

	@Editable(order=100, description="Specify batch commands to execute "
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
		return new WindowsBatchInterpreterFacade(getCommands());
	}

}
