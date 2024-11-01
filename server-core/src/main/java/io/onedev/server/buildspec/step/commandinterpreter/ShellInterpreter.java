package io.onedev.server.buildspec.step.commandinterpreter;

import io.onedev.k8shelper.CommandFacade;
import io.onedev.k8shelper.RegistryLoginFacade;
import io.onedev.k8shelper.ShellFacade;
import io.onedev.server.annotation.Code;
import io.onedev.server.annotation.Editable;
import io.onedev.server.annotation.Interpolative;
import io.onedev.server.model.support.administration.jobexecutor.JobExecutor;

import javax.validation.constraints.NotEmpty;
import java.util.List;
import java.util.Map;

@Editable(order=200, name="Custom Linux Shell")
public class ShellInterpreter extends Interpreter {

	private static final long serialVersionUID = 1L;
	
	private String shell = "bash";

	@Editable(order=100, name="Shell", description="Specify shell to be used")
	@Interpolative(variableSuggester = "suggestVariables")
	@NotEmpty
	public String getShell() {
		return shell;
	}

	public void setShell(String shell) {
		this.shell = shell;
	}

	@Editable(order=110, description="Specify shell commands to execute "
			+ "under the <a href='https://docs.onedev.io/concepts#job-workspace' target='_blank'>job workspace</a>")
	@Interpolative
	@Code(language=Code.SHELL, variableProvider="suggestVariables")
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
		return new ShellFacade(image, runAs, registryLogins, getShell(), getCommands(), 
				envMap, useTTY);
	}

}
