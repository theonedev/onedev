package io.onedev.server.buildspec.step.commandinterpreter;

import java.util.List;

import javax.validation.constraints.Size;

import io.onedev.k8shelper.CommandFacade;
import io.onedev.k8shelper.PowerShellFacade;
import io.onedev.server.util.validation.annotation.Code;
import io.onedev.server.web.editable.annotation.Editable;
import io.onedev.server.web.editable.annotation.Interpolative;

@Editable(order=300, name="PowerShell")
public class PowerShellInterpreter extends Interpreter {

	private static final long serialVersionUID = 1L;

	@Editable(order=110, description="Specify PowerShell commands to execute "
			+ "under the <a href='https://docs.onedev.io/concepts#job-workspace' target='_blank'>job workspace</a>.<br>"
			+ "<b class='text-warning'>NOTE: </b> OneDev checks exit code of the script to determine if step is successful. "
			+ "Since PowerShell always exit with 0 even if there are script errors, you should handle errors in the script "
			+ "and exit with non-zero code, or add line <code>$ErrorActionPreference = &quot;Stop&quot;</code> at start of "
			+ "your script<br>")
	@Interpolative
	@Code(language=Code.POWER_SHELL, variableProvider="suggestVariables")
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
		return new PowerShellFacade(image, getCommands(), useTTY);
	}

}
