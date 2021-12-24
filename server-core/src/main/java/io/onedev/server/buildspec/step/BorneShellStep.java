package io.onedev.server.buildspec.step;

import java.util.List;

import javax.validation.constraints.Size;

import io.onedev.k8shelper.BorneShellExecutable;
import io.onedev.k8shelper.Executable;
import io.onedev.server.buildspec.param.ParamCombination;
import io.onedev.server.model.Build;
import io.onedev.server.util.validation.annotation.Code;
import io.onedev.server.web.editable.annotation.Editable;
import io.onedev.server.web.editable.annotation.Interpolative;

@Editable(order=14, name="Execute Borne Shell Commands")
public class BorneShellStep extends CommandStep {

	private static final long serialVersionUID = 1L;

	@Editable(order=110, description="Specify borne shell commands to execute "
			+ "under the <a href='$docRoot/pages/concepts.md#job-workspace' target='_blank'>job workspace</a>. Depending on the "
			+ "job executor being used, this may be executed either inside or outside container")
	@Interpolative
	@Code(language=Code.SHELL, variableProvider="suggestVariables")
	@Size(min=1, message="may not be empty")
	public List<String> getCommands() {
		return super.getCommands();
	}

	public void setCommands(List<String> commands) {
		super.setCommands(commands);
	}

	@Override
	public Executable getExecutable(Build build, String jobToken, ParamCombination paramCombination) {
		return new BorneShellExecutable(getImage(), getCommands(), isUseTTY());
	}

}
