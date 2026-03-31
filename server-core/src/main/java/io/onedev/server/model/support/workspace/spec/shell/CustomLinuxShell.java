package io.onedev.server.model.support.workspace.spec.shell;

import javax.validation.constraints.NotEmpty;

import io.onedev.k8shelper.LinuxShellFacility;
import io.onedev.k8shelper.ShellFacility;
import io.onedev.server.annotation.Code;
import io.onedev.server.annotation.Editable;
import io.onedev.server.annotation.Interpolative;

@Editable(order=200, name="Custom Linux Shell")
public class CustomLinuxShell extends WorkspaceShell {

	private static final long serialVersionUID = 1L;
	
	private String shell = "bash";

	@Editable(order=100, description="Specify shell executable to be used")
	@Interpolative(variableSuggester = "suggestVariables")
	@NotEmpty
	public String getShell() {
		return shell;
	}

	public void setShell(String shell) {
		this.shell = shell;
	}

	@Editable(order=200, description="""
		Optionally specify shell commands to run to set up the workspace. These commands will 
		run from working directory holding cloned repository files""")
	@Interpolative
	@Code(language=Code.SHELL, variableProvider="suggestVariables")
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
		return new LinuxShellFacility(shell);
	}
	
}
