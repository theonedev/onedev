package io.onedev.server.buildspec.step.commandinterpreter;

import io.onedev.k8shelper.CommandFacade;
import io.onedev.k8shelper.PowerShellFacade;
import io.onedev.k8shelper.RegistryLoginFacade;
import io.onedev.server.annotation.Code;
import io.onedev.server.annotation.Editable;
import io.onedev.server.annotation.Interpolative;
import io.onedev.server.model.support.administration.jobexecutor.JobExecutor;

import javax.validation.constraints.NotEmpty;
import java.util.List;
import java.util.Map;

@Editable(order=300, name="PowerShell")
public class PowerShellInterpreter extends Interpreter {

	private static final long serialVersionUID = 1L;
	
	private String powershell = "powershell.exe";

	@Editable(order=100, name="Executable", description="Specify powershell executable to be used")
	@Interpolative(variableSuggester = "suggestVariables")
	@NotEmpty
	public String getPowershell() {
		return powershell;
	}

	public void setPowershell(String powershell) {
		this.powershell = powershell;
	}

	@Editable(order=110, description="Specify PowerShell commands to execute "
			+ "under the <a href='https://docs.onedev.io/concepts#job-workspace' target='_blank'>job workspace</a>.<br>"
			+ "<b class='text-warning'>NOTE: </b> OneDev checks exit code of the script to determine if step is successful. "
			+ "Since PowerShell always exit with 0 even if there are script errors, you should handle errors in the script "
			+ "and exit with non-zero code, or add line <code>$ErrorActionPreference = &quot;Stop&quot;</code> at start of "
			+ "your script<br>")
	@Interpolative
	@Code(language=Code.POWER_SHELL, variableProvider="suggestVariables")
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
	public CommandFacade getExecutable(JobExecutor jobExecutor, String jobToken, String image,
									   String runAs, List<RegistryLoginFacade> registryLogins,
									   Map<String, String> envMap, boolean useTTY) {
		return new PowerShellFacade(image, runAs, registryLogins, getPowershell(), getCommands(), envMap, useTTY);
	}

}
