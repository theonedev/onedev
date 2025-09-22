package io.onedev.server.buildspec;

import static java.util.stream.Collectors.toList;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;

import io.onedev.commons.codeassist.InputCompletion;
import io.onedev.commons.codeassist.InputStatus;
import io.onedev.commons.codeassist.InputSuggestion;
import io.onedev.k8shelper.ServiceFacade;
import io.onedev.server.annotation.DnsName;
import io.onedev.server.annotation.Editable;
import io.onedev.server.annotation.Interpolative;
import io.onedev.server.annotation.RegEx;
import io.onedev.server.annotation.SuggestionProvider;
import io.onedev.server.buildspec.job.EnvVar;
import io.onedev.server.buildspec.step.RegistryLogin;
import io.onedev.server.model.Build;

@Editable
public class Service implements NamedElement {

	private static final long serialVersionUID = 1L;

	private String name;
	
	private String image;
	
	private String arguments;
	
	private List<EnvVar> envVars = new ArrayList<>();
	
	private String readinessCheckCommand;
	
	private String runAs;

	private List<RegistryLogin> registryLogins = new ArrayList<>();
	
	@Editable(order=100, description="Specify name of the service, which will be used as host name to access the service")
	@SuggestionProvider("getNameSuggestions")
	@DnsName
	@NotEmpty
	@Override
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	@SuppressWarnings("unused")
	private static List<InputCompletion> getNameSuggestions(InputStatus status) {
		BuildSpec buildSpec = BuildSpec.get();
		if (buildSpec != null) {
			List<String> candidates = new ArrayList<>(buildSpec.getServiceMap().keySet());
			buildSpec.getServices().forEach(it->candidates.remove(it.getName()));
			return BuildSpec.suggestOverrides(candidates, status);
		}
		return new ArrayList<>();
	}

	@Editable(order=200, description="Specify docker image of the service")
	@Interpolative(variableSuggester="suggestVariables")
	@NotEmpty
	public String getImage() {
		return image;
	}

	public void setImage(String image) {
		this.image = image;
	}

	@Editable(order=220, description="Optionally specify arguments to run above image")
	@Interpolative(variableSuggester="suggestVariables")
	public String getArguments() {
		return arguments;
	}

	public void setArguments(String arguments) {
		this.arguments = arguments;
	}

	@Editable(order=300, name="Environment Variables", description="Optionally specify environment variables of "
			+ "the service")
	@Valid
	public List<EnvVar> getEnvVars() {
		return envVars;
	}

	public void setEnvVars(List<EnvVar> envVars) {
		this.envVars = envVars;
	}

	@Editable(order=400, description="Specify command to check readiness of the service. This command will "
			+ "be interpretated by cmd.exe on Windows images, and by shell on Linux images. It will be "
			+ "executed repeatedly until a zero code is returned to indicate service ready")
	@Interpolative(variableSuggester="suggestVariables")
	@NotEmpty
	public String getReadinessCheckCommand() {
		return readinessCheckCommand;
	}

	public void setReadinessCheckCommand(String readinessCheckCommand) {
		this.readinessCheckCommand = readinessCheckCommand;
	}

	@Editable(order=450, name="Run As", group = "More Settings", placeholder = "root", description = "Optionally specify uid:gid to run container as. " +
			"<b class='text-warning'>Note:</b> This setting should be left empty if container runtime is rootless or " +
			"using user namespace remapping")
	@RegEx(pattern="\\d+:\\d+", message = "Should be specified in form of <uid>:<gid>")
	public String getRunAs() {
		return runAs;
	}

	public void setRunAs(String runAs) {
		this.runAs = runAs;
	}

	@Editable(order=475, group="More Settings", description="Optionally specify registry logins to override " +
			"those defined in job executor. For built-in registry, use <code>@server_url@</code> for registry url, " +
			"<code>@job_token@</code> for user name, and access token secret for password secret")
	public List<RegistryLogin> getRegistryLogins() {
		return registryLogins;
	}

	public void setRegistryLogins(List<RegistryLogin> registryLogins) {
		this.registryLogins = registryLogins;
	}
	
	@SuppressWarnings("unused")
	private static List<InputSuggestion> suggestVariables(String matchWith) {
		return BuildSpec.suggestVariables(matchWith, false, false, false);
	}
	
	public ServiceFacade getFacade(Build build, String jobToken) {
		var envs = new HashMap<String, String>();
		for (var envVar: getEnvVars())
			envs.put(envVar.getName(), envVar.getValue());
		var registryLogins = getRegistryLogins().stream().map(it->it.getFacade(build)).collect(toList());
		return new ServiceFacade(getName(), getImage(), getRunAs(), getArguments(), envs,
				getReadinessCheckCommand(), registryLogins);		
	}
	
}
