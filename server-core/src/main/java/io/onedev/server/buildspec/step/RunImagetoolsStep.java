package io.onedev.server.buildspec.step;

import io.onedev.commons.codeassist.InputSuggestion;
import io.onedev.k8shelper.RunImagetoolsFacade;
import io.onedev.k8shelper.StepFacade;
import io.onedev.server.OneDev;
import io.onedev.server.annotation.ChoiceProvider;
import io.onedev.server.annotation.Editable;
import io.onedev.server.annotation.Interpolative;
import io.onedev.server.buildspec.BuildSpec;
import io.onedev.server.buildspec.param.ParamCombination;
import io.onedev.server.entitymanager.SettingManager;
import io.onedev.server.model.Build;
import io.onedev.server.model.Project;
import io.onedev.server.model.support.administration.jobexecutor.JobExecutor;
import io.onedev.server.util.UrlUtils;

import javax.validation.constraints.NotEmpty;
import java.util.List;

import static io.onedev.server.buildspec.step.StepGroup.DOCKER_IMAGE;
import static java.util.stream.Collectors.toList;

@Editable(order=230, name="Run Docker Buildx Image Tools", group = DOCKER_IMAGE, description="Run docker buildx imagetools " +
		"command with specified arguments. This step can only be executed by server docker executor " +
		"or remote docker executor")
public class RunImagetoolsStep extends Step {

	private static final long serialVersionUID = 1L;
	
	private String arguments;
	
	private String builtInRegistryAccessTokenSecret;

	@Editable(order=100, description="Specify arguments for imagetools. For instance " +
			"<code>create -t myorg/myrepo:1.0.0 myorg/myrepo@&lt;arm64 manifest digest&gt; myorg/myrepo@&lt;amd64 manifest digest&gt;</code>")
	@Interpolative(variableSuggester="suggestVariables")
	@NotEmpty
	public String getArguments() {
		return arguments;
	}

	public void setArguments(String arguments) {
		this.arguments = arguments;
	}
	
	@Editable(order=200, name="Built-in Registry Access Token Secret", group = "More Settings", descriptionProvider = "getBuiltInRegistryAccessTokenSecretDescription")
	@ChoiceProvider("getAccessTokenSecretChoices")
	public String getBuiltInRegistryAccessTokenSecret() {
		return builtInRegistryAccessTokenSecret;
	}

	public void setBuiltInRegistryAccessTokenSecret(String builtInRegistryAccessTokenSecret) {
		this.builtInRegistryAccessTokenSecret = builtInRegistryAccessTokenSecret;
	}

	private static List<String> getAccessTokenSecretChoices() {
		return Project.get().getHierarchyJobSecrets()
				.stream().map(it->it.getName()).distinct().collect(toList());
	}

	private static String getBuiltInRegistryAccessTokenSecretDescription() {
		var serverUrl = OneDev.getInstance(SettingManager.class).getSystemSetting().getServerUrl();
		var server = UrlUtils.getServer(serverUrl);
		return "Optionally specify a secret to be used as access token for built-in registry server " +
				"<code>" + server + "</code>";
	}
	
	static List<InputSuggestion> suggestVariables(String matchWith) {
		return BuildSpec.suggestVariables(matchWith, true, true, false);
	}
	
	@Override
	public StepFacade getFacade(Build build, JobExecutor jobExecutor, String jobToken, ParamCombination paramCombination) {
		String accessToken;
		if (getBuiltInRegistryAccessTokenSecret() != null)
			accessToken = build.getJobAuthorizationContext().getSecretValue(getBuiltInRegistryAccessTokenSecret());
		else
			accessToken = null;
		return new RunImagetoolsFacade(getArguments(), accessToken);
	}
	
}
