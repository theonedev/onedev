package io.onedev.server.model.support.workspace.spec.shell;

import javax.validation.constraints.NotEmpty;

import io.onedev.k8shelper.PosixFacility;
import io.onedev.k8shelper.ShellFacility;
import io.onedev.server.annotation.Code;
import io.onedev.server.annotation.Editable;
import io.onedev.server.annotation.Interpolative;

@Editable(order=100, name="POSIX Compatible Shell")
public class PosixShell extends WorkspaceShell {

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

	@Editable(order=200, descriptionProvider="getSetupCommandDescription")
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
		return new PosixFacility(shell);
	}

	@Override
	public String decorateRunPromptCommand(String command, String prompt,
										   String successMarker, String failureMarker) {
		return "export TASK_PROMPT=" + quote(prompt) + "\n"
				+ CHECK_TOD_VERSION_COMMAND + " && ( " + command + " )\n"
				+ "if [ $? -eq 0 ]; then " + printMarker(successMarker)
				+ "; else " + printMarker(failureMarker) + "; fi";
	}

	private String quote(String value) {
		return "'" + value.replace("'", "'\"'\"'") + "'";
	}

	private String printMarker(String marker) {
		var splitIndex = marker.lastIndexOf('_', marker.length() - 3) + 1;
		return "printf '%s\\n' " + quote(marker.substring(0, splitIndex)) + quote(marker.substring(splitIndex));
	}

}
