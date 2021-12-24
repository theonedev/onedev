package io.onedev.server.buildspec.step;

import java.util.List;

import javax.validation.constraints.Size;

import io.onedev.k8shelper.Executable;
import io.onedev.k8shelper.PowerShellExecutable;
import io.onedev.server.buildspec.param.ParamCombination;
import io.onedev.server.model.Build;
import io.onedev.server.util.validation.annotation.Code;
import io.onedev.server.web.editable.annotation.Editable;
import io.onedev.server.web.editable.annotation.Interpolative;

@Editable(order=17, name="Execute PowerShell Commands")
public class PowerShellStep extends CommandStep {

	private static final long serialVersionUID = 1L;

	@Editable(order=110, description="Specify PowerShell commands to execute "
			+ "under the <a href='$docRoot/pages/concepts.md#job-workspace' target='_blank'>job workspace</a>. Depending on the "
			+ "job executor being used, this may be executed either inside or outside container.<br>"
			+ "<b class='text-danger'>NOTE: </b> OneDev checks exit code of the script to determine if step is successful. "
			+ "Since PowerShell always exit with 0 even if there are script errors, you should handle errors in the script "
			+ "and exit with non-zero code, or simply add line <code>$ErrorActionPreference = \"Stop\"</code> at start of "
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
	public Executable getExecutable(Build build, String jobToken, ParamCombination paramCombination) {
		return new PowerShellExecutable(getImage(), getCommands(), isUseTTY());
	}
	
}
