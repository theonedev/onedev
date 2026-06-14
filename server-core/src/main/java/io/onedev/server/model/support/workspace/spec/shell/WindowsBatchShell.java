package io.onedev.server.model.support.workspace.spec.shell;

import io.onedev.k8shelper.ShellFacility;
import io.onedev.k8shelper.WindowsBatchFacility;
import io.onedev.server.annotation.Code;
import io.onedev.server.annotation.Editable;
import io.onedev.server.annotation.Interpolative;

@Editable(order=200, name="Windows Batch")
public class WindowsBatchShell extends WorkspaceShell {

	private static final long serialVersionUID = 1L;

	@Editable(order=100, descriptionProvider="getSetupCommandDescription")
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
		return new WindowsBatchFacility();
	}

	@Override
	public String decorateRunPromptCommand(String command, String prompt,
										   String successMarker, String failureMarker) {
		return "@setlocal DisableDelayedExpansion\n"
				+ "@set \"TASK_PROMPT=" + escape(prompt) + "\"\n"
				+ "@" + CHECK_TOD_VERSION_COMMAND + " && ( " + command + " )\n"
				+ "@if errorlevel 1 (@" + printMarker(failureMarker)
				+ ") else (@" + printMarker(successMarker) + ")";
	}

	private String escape(String value) {
		return value.replace("^", "^^")
				.replace("%", "%%")
				.replace("\"", "^\"");
	}

	private String printMarker(String marker) {
		var splitIndex = marker.lastIndexOf('_', marker.length() - 3) + 1;
		return "echo " + marker.substring(0, splitIndex) + "^" + marker.substring(splitIndex);
	}

}
