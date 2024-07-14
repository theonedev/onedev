package io.onedev.server.buildspec.step;

import io.onedev.commons.codeassist.InputSuggestion;
import io.onedev.k8shelper.StepFacade;
import io.onedev.server.OneDev;
import io.onedev.server.annotation.*;
import io.onedev.server.buildspec.BuildSpec;
import io.onedev.server.buildspec.param.ParamCombination;
import io.onedev.server.buildspec.step.commandinterpreter.DefaultInterpreter;
import io.onedev.server.buildspec.step.commandinterpreter.Interpreter;
import io.onedev.server.entitymanager.SettingManager;
import io.onedev.server.model.Build;
import io.onedev.server.model.Project;
import io.onedev.server.model.support.administration.jobexecutor.JobExecutor;
import io.onedev.server.util.EditContext;
import io.onedev.server.util.UrlUtils;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.stream.Collectors;

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
	
	private String builtInRegistryAccessTokenSecret;

	private boolean useTTY = true;
	
	@Editable(order=50, description="Whether or not to run this step inside container")
	public boolean isRunInContainer() {
		return runInContainer;
	}

	public void setRunInContainer(boolean runInContainer) {
		this.runInContainer = runInContainer;
	}

	@SuppressWarnings("unused")
	private static boolean isRunInContainerEnabled() {
		return (boolean) EditContext.get().getInputValue("runInContainer");
	}
	
	@Editable(order=100, description="Specify container image to execute commands inside")
	@ShowCondition("isRunInContainerEnabled")
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
	@ShowCondition("isRunInContainerEnabled")
	@RegEx(pattern="\\d+:\\d+", message = "Should be specified in form of <uid>:<gid>")
	public String getRunAs() {
		return runAs;
	}

	public void setRunAs(String runAs) {
		this.runAs = runAs;
	}
	
	@Editable(order=9000, name="Built-in Registry Access Token Secret", group = "More Settings",
			descriptionProvider = "getBuiltInRegistryAccessTokenSecretDescription")
	@ChoiceProvider("getAccessTokenSecretChoices")
	@ShowCondition("isRunInContainerEnabled")
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

	private static String getBuiltInRegistryAccessTokenSecretDescription() {
		var serverUrl = OneDev.getInstance(SettingManager.class).getSystemSetting().getServerUrl();
		var server = UrlUtils.getServer(serverUrl);
		return "Optionally specify a <a href='https://docs.onedev.io/tutorials/cicd/job-secrets' target='_blank'>job secret</a> to be used as access token for built-in registry server " +
				"<code>" + server + "</code>";
	}
	
	@Editable(order=10000, name="Enable TTY Mode", group = "More Settings", description=USE_TTY_HELP)
	@ShowCondition("isRunInContainerEnabled")
	public boolean isUseTTY() {
		return useTTY;
	}

	public void setUseTTY(boolean useTTY) {
		this.useTTY = useTTY;
	}
	
	@Override
	public StepFacade getFacade(Build build, JobExecutor jobExecutor, String jobToken, ParamCombination paramCombination) {
		if (isRunInContainer()) {
			String builtInRegistryAccessToken;
			if (getBuiltInRegistryAccessTokenSecret() != null)
				builtInRegistryAccessToken = build.getJobAuthorizationContext().getSecretValue(getBuiltInRegistryAccessTokenSecret());
			else
				builtInRegistryAccessToken = null;
			return getInterpreter().getExecutable(jobExecutor, jobToken, getImage(), runAs, builtInRegistryAccessToken, isUseTTY());
		} else {
			return getInterpreter().getExecutable(jobExecutor, jobToken, null, null, null, isUseTTY());
		}
	}
	
}
