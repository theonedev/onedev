package io.onedev.server.buildspec.step.command.interpreter;

import java.util.List;

import javax.validation.constraints.Size;

import io.onedev.k8shelper.CommandExecutable;
import io.onedev.k8shelper.PowerShellExecutable;
import io.onedev.server.util.validation.annotation.Code;
import io.onedev.server.web.editable.annotation.Editable;
import io.onedev.server.web.editable.annotation.Interpolative;

@Editable(order=300, name="PowerShell")
public class PowerShellInterpreter extends Interpreter {

	private static final long serialVersionUID = 1L;

	@Editable(order=110, description="Specify PowerShell commands to execute "
			+ "under the <a href='$docRoot/pages/concepts.md#job-workspace' target='_blank'>job workspace</a>.<br>"
			+ "<b class='text-warning'>NOTE: </b> OneDev checks exit code of the script to determine if step is successful. "
			+ "Since PowerShell always exit with 0 even if there are script errors, you should handle errors in the script "
			+ "and exit with non-zero code, or add line <code>$ErrorActionPreference = &quot;Stop&quot;</code> at start of "
			+ "your script<br>")
	@Interpolative
	@Code(language=Code.POWER_SHELL, variableProvider="suggestVariables")
	@Size(min=1, message="may not be empty")
	public List<String> getCommands() {
		return super.getCommands();
	}

	public void setCommands(List<String> commands) {
		super.setCommands(commands);
	}
	
	@Override
	public CommandExecutable getExecutable(String image, boolean useTTY) {
		return new PowerShellExecutable(image, getCommands(), useTTY);
	}

}
