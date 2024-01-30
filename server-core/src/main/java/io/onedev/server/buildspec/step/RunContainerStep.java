package io.onedev.server.buildspec.step;

import io.onedev.commons.codeassist.InputSuggestion;
import io.onedev.k8shelper.RunContainerFacade;
import io.onedev.k8shelper.StepFacade;
import io.onedev.server.OneDev;
import io.onedev.server.SubscriptionManager;
import io.onedev.server.annotation.ChoiceProvider;
import io.onedev.server.annotation.Editable;
import io.onedev.server.annotation.Interpolative;
import io.onedev.server.buildspec.BuildSpec;
import io.onedev.server.buildspec.job.EnvVar;
import io.onedev.server.buildspec.param.ParamCombination;
import io.onedev.server.model.Build;
import io.onedev.server.model.Project;
import io.onedev.server.model.support.administration.jobexecutor.JobExecutor;

import javax.annotation.Nullable;
import javax.validation.constraints.NotEmpty;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Editable(order=150, name="Run Docker Container", description="Run specified docker container. <a href='https://docs.onedev.io/concepts#job-workspace' target='_blank'>Job workspace</a> "
		+ "is mounted into the container and its path is placed in environment variable <code>ONEDEV_WORKSPACE</code>. " +
		"<b class='text-warning'>Note: </b> this step can only be executed by server docker executor or remote " +
		"docker executor")
public class RunContainerStep extends Step {

	private static final long serialVersionUID = 1L;

	private String image;
	
	private String args;
	
	private List<EnvVar> envVars = new ArrayList<>();

	private String workingDir;
	
	private List<VolumeMount> volumeMounts = new ArrayList<>();

	private String builtInRegistryAccessTokenSecret;
	
	private boolean useTTY;
	
	@Editable(order=100, description="Specify container image to run")
	@Interpolative(variableSuggester="suggestVariables")
	@NotEmpty
	public String getImage() {
		return image;
	}

	public void setImage(String image) {
		this.image = image;
	}

	@Editable(order=200, name="Arguments", description="Optionally specify container arguments separated by space. " +
			"Single argument containing space should be quoted. <b class='text-warning'>Note: </b> do not confuse " +
			"this with container options which should be specified in executor setting")
	@Interpolative(variableSuggester="suggestVariables")
	public String getArgs() {
		return args;
	}

	public void setArgs(String args) {
		this.args = args;
	}

	@Editable(order=300, name="Working Directory", description="Optionally specify working directory of the container. "
			+ "Leave empty to use default working directory of the container")
	@Interpolative(variableSuggester="suggestVariables")
	@Nullable
	public String getWorkingDir() {
		return workingDir;
	}

	public void setWorkingDir(String workingDir) {
		this.workingDir = workingDir;
	}

	@Editable(order=400, name="Environment Variables", description="Optionally specify environment "
			+ "variables for the container")
	public List<EnvVar> getEnvVars() {
		return envVars;
	}

	public void setEnvVars(List<EnvVar> envVars) {
		this.envVars = envVars;
	}

	@Editable(order=500, group = "More Settings", description="Optionally mount directories or files under job workspace into container")
	public List<VolumeMount> getVolumeMounts() {
		return volumeMounts;
	}

	public void setVolumeMounts(List<VolumeMount> volumeMounts) {
		this.volumeMounts = volumeMounts;
	}

	@Editable(order=600, name="Built-in Registry Access Token Secret", group = "More Settings", description="Optionally specify a " +
			"access token secret to access built-in container registry if necessary. If this step needs to " +
			"access external container registry, login information should be configured in corresponding " +
			"executor then")
	@ChoiceProvider("getAccessTokenSecretChoices")
	public String getBuiltInRegistryAccessTokenSecret() {
		return builtInRegistryAccessTokenSecret;
	}

	public void setBuiltInRegistryAccessTokenSecret(String builtInRegistryAccessTokenSecret) {
		this.builtInRegistryAccessTokenSecret = builtInRegistryAccessTokenSecret;
	}

	protected static List<String> getAccessTokenSecretChoices() {
		return Project.get().getHierarchyJobSecrets()
				.stream().map(it->it.getName()).collect(Collectors.toList());
	}
	
	private static boolean isSubscriptionActive() {
		return OneDev.getInstance(SubscriptionManager.class).isSubscriptionActive();
	}
	
	@Editable(order=10000, name="Enable TTY Mode", group = "More Settings", description="Many commands print outputs with ANSI colors in "
			+ "TTY mode to help identifying problems easily. However some commands running in this mode may "
			+ "wait for user input to cause build hanging. This can normally be fixed by adding extra options "
			+ "to the command")
	public boolean isUseTTY() {
		return useTTY;
	}

	public void setUseTTY(boolean useTTY) {
		this.useTTY = useTTY;
	}

	static List<InputSuggestion> suggestVariables(String matchWith) {
		return BuildSpec.suggestVariables(matchWith, false, false, false);
	}

	@Override
	public StepFacade getFacade(Build build, JobExecutor jobExecutor, String jobToken, ParamCombination paramCombination) {
		Map<String, String> envMap = new HashMap<>();
		for (EnvVar var: getEnvVars())
			envMap.put(var.getName(), var.getValue());
		Map<String, String> mountMap = new HashMap<>();
		for (VolumeMount mount: getVolumeMounts()) {
			var sourcePath = mount.getSourcePath();
			if (sourcePath == null)
				sourcePath = ".";
			mountMap.put(sourcePath, mount.getTargetPath());
		}
		
		String builtInRegistryAccessToken;
		if (getBuiltInRegistryAccessTokenSecret() != null)
			builtInRegistryAccessToken = build.getJobAuthorizationContext().getSecretValue(getBuiltInRegistryAccessTokenSecret());
		else 
			builtInRegistryAccessToken = null;
		return new RunContainerFacade(getImage(), null, getArgs(), envMap, getWorkingDir(), mountMap, 
				builtInRegistryAccessToken, isUseTTY());
	}
	
}
