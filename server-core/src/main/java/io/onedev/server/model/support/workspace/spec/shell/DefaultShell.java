package io.onedev.server.model.support.workspace.spec.shell;

import io.onedev.k8shelper.DefaultShellFacility;
import io.onedev.k8shelper.ShellFacility;
import io.onedev.server.annotation.Code;
import io.onedev.server.annotation.Editable;
import io.onedev.server.annotation.Interpolative;

@Editable(order=100, name="Default (sh on Linux, batch on Windows)")
public class DefaultShell extends WorkspaceShell {

	private static final long serialVersionUID = 1L;

	@Editable(order=100, description="""
		Optionally specify sh commands (on Linux/Unix) or batch commands (on Windows) 
		to run to set up the workspace. These commands will run from working directory
		holding cloned repository files""")
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
		return new DefaultShellFacility();
	}

}
