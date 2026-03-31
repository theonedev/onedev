package io.onedev.server.model.support.workspace.spec.shell;

import javax.validation.constraints.NotEmpty;

import io.onedev.k8shelper.PowerShellFacility;
import io.onedev.k8shelper.ShellFacility;
import io.onedev.server.annotation.Code;
import io.onedev.server.annotation.Editable;
import io.onedev.server.annotation.Interpolative;

@Editable(order=300, name="PowerShell")
public class PowerShell extends WorkspaceShell {

	private static final long serialVersionUID = 1L;
	
	private String powershell = "powershell.exe";
	
	@Editable(order=100,  description="Specify powershell executable to be used")
	@Interpolative(variableSuggester = "suggestVariables")
	@NotEmpty
	public String getPowershell() {
		return powershell;
	}

	public void setPowershell(String powershell) {
		this.powershell = powershell;
	}

	@Editable(order=100, description="""
		Optionally specify PowerShell commands to run to set up the workspace. These commands 
		will run from working directory holding cloned repository files""")
	@Code(language=Code.POWER_SHELL, variableProvider="suggestVariables")
	@Interpolative
	@Override
	public String getSetupCommands() {
		return super.getSetupCommands();
	}

	@Override
	public void setSetupCommands(String setupCommands) {
		super.setSetupCommands(setupCommands);
	}

	@Override
	public ShellFacility getFacility() {
		return new PowerShellFacility(powershell);
	}

}
