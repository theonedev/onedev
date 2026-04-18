package io.onedev.server.model.support.workspace.spec.shell;

import static io.onedev.server.web.translation.Translation._T;

import java.io.Serializable;
import java.util.List;

import io.onedev.commons.codeassist.InputSuggestion;
import io.onedev.k8shelper.ShellFacility;
import io.onedev.server.annotation.Editable;
import io.onedev.server.web.util.SuggestionUtils;

@Editable
public abstract class WorkspaceShell implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private String setupCommands;
	
	public String getSetupCommands() {
		return setupCommands;
	}

	public void setSetupCommands(String setupCommands) {
		this.setupCommands = setupCommands;
	}

	public abstract ShellFacility getFacility();

	protected static List<InputSuggestion> suggestVariables(String matchWith) {
		return SuggestionUtils.suggestWorkspaceVariables(matchWith);
	}

	protected static String getSetupCommandDescription() {
		return _T("""
			Optionally specify shell commands to run to set up the workspace. These commands will 
			run from working directory holding cloned repository files. It runs after cache and 
			user data restored and config files generated""");
	}
	
}
