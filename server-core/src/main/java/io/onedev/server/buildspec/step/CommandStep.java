package io.onedev.server.buildspec.step;

import static java.util.stream.Collectors.toList;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import io.onedev.commons.codeassist.InputSuggestion;
import io.onedev.k8shelper.StepFacade;
import io.onedev.server.annotation.DependsOn;
import io.onedev.server.annotation.Editable;
import io.onedev.server.annotation.Interpolative;
import io.onedev.server.annotation.RegEx;
import io.onedev.server.buildspec.BuildSpec;
import io.onedev.server.buildspec.job.EnvVar;
import io.onedev.server.buildspec.param.ParamCombination;
import io.onedev.server.buildspec.step.commandinterpreter.DefaultInterpreter;
import io.onedev.server.buildspec.step.commandinterpreter.Interpreter;
import io.onedev.server.model.Build;
import io.onedev.server.model.support.administration.jobexecutor.DockerAware;
import io.onedev.server.model.support.administration.jobexecutor.JobExecutor;

@Editable(order=100, name="Execute Commands")
public class CommandStep extends Step {

	private static final long serialVersionUID = 1L;
	
	public static final String USE_TTY_HELP = "Many commands print outputs with ANSI colors in "
			+ "TTY mode to help identifying problems easily. However some commands running in this mode may "
			+ "wait for user input to cause build hanging. This can normally be fixed by adding extra options "
			+ "to the command";

	private boolean runInContainer = true;
	
	private String image;
	
	private Interpreter interpreter = new DefaultInterpreter();
	
	private String runAs;
	
	private List<RegistryLogin> registryLogins = new ArrayList<>();
	
	private List<EnvVar> envVars = new ArrayList<>();
	
	private boolean useTTY = true;
	
	@Editable(order=50, description="Whether or not to run this step inside container")
	public boolean isRunInContainer() {
		return runInContainer;
	}

	public void setRunInContainer(boolean runInContainer) {
		this.runInContainer = runInContainer;
	}
	
	@Editable(order=100, name="container:image", description="Specify container image to execute commands inside")
	@DependsOn(property="runInContainer")
	@Interpolative(variableSuggester="suggestVariables")
	@NotEmpty
	public String getImage() {
		return image;
	}

	public void setImage(String image) {
		this.image = image;
	}
	
	static List<InputSuggestion> suggestVariables(String matchWith) {
		return BuildSpec.suggestVariables(matchWith, false, false, false);
	}
	
	@Editable(order=110)
	@Valid
	@NotNull
	public Interpreter getInterpreter() {
		return interpreter;
	}

	public void setInterpreter(Interpreter interpreter) {
		this.interpreter = interpreter;
	}

	@Editable(order=8000, name="Run As", group = "More Settings", placeholder = "root", description = "Optionally specify uid:gid to run container as. " +
			"<b class='text-warning'>Note:</b> This setting should be left empty if container runtime is rootless or " +
			"using user namespace remapping")
	@DependsOn(property="runInContainer")
	@RegEx(pattern="\\d+:\\d+", message = "Should be specified in form of <uid>:<gid>")
	public String getRunAs() {
		return runAs;
	}

	public void setRunAs(String runAs) {
		this.runAs = runAs;
	}

	@Editable(order=8500, group="More Settings", description="Optionally specify registry logins to override " +
			"those defined in job executor. For built-in registry, use <code>@server_url@</code> for registry url, " +
			"<code>@job_token@</code> for user name, and access token secret for password secret")
	@DependsOn(property="runInContainer")
	@Valid
	public List<RegistryLogin> getRegistryLogins() {
		return registryLogins;
	}

	public void setRegistryLogins(List<RegistryLogin> registryLogins) {
		this.registryLogins = registryLogins;
	}
	
	@Editable(order=9900, name="Environment Variables", group="More Settings", description="Optionally specify environment "
			+ "variables for this step")
	@Valid
	public List<EnvVar> getEnvVars() {
		return envVars;
	}

	public void setEnvVars(List<EnvVar> envVars) {
		this.envVars = envVars;
	}
	
	@Editable(order=10000, name="Enable TTY Mode", group = "More Settings", description=USE_TTY_HELP)
	@DependsOn(property="runInContainer")
	public boolean isUseTTY() {
		return useTTY;
	}

	public void setUseTTY(boolean useTTY) {
		this.useTTY = useTTY;
	}
	
	@Override
	public StepFacade getFacade(Build build, JobExecutor jobExecutor, String jobToken, ParamCombination paramCombination) {
		var envMap = new HashMap<String, String>();
		for (var envVar: envVars)
			envMap.put(envVar.getName(), envVar.getValue());
		
		if (isRunInContainer()) {
			var registryLogins = getRegistryLogins().stream().map(it->it.getFacade(build)).collect(toList());
			return getInterpreter().getExecutable(jobExecutor, jobToken, getImage(), runAs, registryLogins, envMap, isUseTTY());
		} else {
			return getInterpreter().getExecutable(jobExecutor, jobToken, null, null, new ArrayList<>(), envMap, isUseTTY());
		}
	}

	@Override
	public boolean isApplicable(Build build, JobExecutor executor) {
		if (isRunInContainer()) 
			return executor instanceof DockerAware;
		else 
			return !(executor instanceof DockerAware);
	}

}
