package io.onedev.server.buildspec.step;

import io.onedev.commons.codeassist.InputSuggestion;
import io.onedev.k8shelper.BuildImageFacade;
import io.onedev.k8shelper.StepFacade;
import io.onedev.server.OneDev;
import io.onedev.server.SubscriptionManager;
import io.onedev.server.annotation.*;
import io.onedev.server.buildspec.BuildSpec;
import io.onedev.server.buildspec.param.ParamCombination;
import io.onedev.server.entitymanager.SettingManager;
import io.onedev.server.model.Build;
import io.onedev.server.model.Project;
import io.onedev.server.model.support.administration.jobexecutor.JobExecutor;
import io.onedev.server.util.UrlUtils;
import io.onedev.server.web.util.WicketUtils;

import javax.validation.constraints.NotEmpty;
import java.util.List;

import static java.util.stream.Collectors.toList;

@Editable(order=160, name="Build Docker Image", description="Build and publish docker image with docker buildx. " +
		"This step can only be executed by server docker executor or remote docker executor. To build image with " +
		"Kubernetes executor, please use kaniko step instead")
public class BuildImageStep extends Step {

	private static final long serialVersionUID = 1L;

	private String buildPath;
	
	private String dockerfile;
	
	private String tags;

	private boolean publish = true;
	
	private String builtInRegistryAccessTokenSecret;
	
	private boolean removeDanglingImages = true;
	
	private String moreOptions;
	
	@Editable(order=100, description="Optionally specify build path relative to <a href='https://docs.onedev.io/concepts#job-workspace' target='_blank'>job workspace</a>. "
			+ "Leave empty to use job workspace itself")
	@Interpolative(variableSuggester="suggestVariables")
	@SubPath
	public String getBuildPath() {
		return buildPath;
	}

	public void setBuildPath(String buildPath) {
		this.buildPath = buildPath;
	}

	@Editable(order=200, description="Optionally specify Dockerfile relative to <a href='https://docs.onedev.io/concepts#job-workspace' target='_blank'>job workspace</a>. "
			+ "Leave empty to use file <tt>Dockerfile</tt> under build path specified above")
	@Interpolative(variableSuggester="suggestVariables")
	@SubPath
	public String getDockerfile() {
		return dockerfile;
	}

	public void setDockerfile(String dockerfile) {
		this.dockerfile = dockerfile;
	}

	@Editable(order=300, description="Specify full tag of the image, for instance <tt>myorg/myrepo:latest</tt>, "
			+ "<tt>myorg/myrepo:1.0.0</tt>, or <tt>myregistry:5000/myorg/myrepo:1.0.0</tt>. "
			+ "Multiple tags should be separated with space.<br>")
	@Interpolative(variableSuggester="suggestVariables")
	@NotEmpty
	public String getTags() {
		return tags;
	}

	public void setTags(String tags) {
		this.tags = tags;
	}

	@Editable(order=330, name="Publish After Build", description="Whether or not to publish built image to docker registry")
	public boolean isPublish() {
		return publish;
	}

	public void setPublish(boolean publish) {
		this.publish = publish;
	}

	@Editable(order=335, name="Built-in Registry Access Token Secret", descriptionProvider = "getBuiltInRegistryAccessTokenSecretDescription")
	@ShowCondition("isSubscriptionActive")
	@ChoiceProvider("getAccessTokenSecretChoices")
	public String getBuiltInRegistryAccessTokenSecret() {
		return builtInRegistryAccessTokenSecret;
	}

	public void setBuiltInRegistryAccessTokenSecret(String builtInRegistryAccessTokenSecret) {
		this.builtInRegistryAccessTokenSecret = builtInRegistryAccessTokenSecret;
	}
	
	private static boolean isSubscriptionActive() {
		return OneDev.getInstance(SubscriptionManager.class).isSubscriptionActive();
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
	
	@Editable(order=340, name="Remove Dangling Images After Build")
	public boolean isRemoveDanglingImages() {
		return removeDanglingImages;
	}

	public void setRemoveDanglingImages(boolean removeDanglingImages) {
		this.removeDanglingImages = removeDanglingImages;
	}

	@Editable(order=350, description="Optionally specify additional options to build image, " +
			"separated by spaces. For instance <code>--builder</code> and <code>--platform</code> can be " +
			"used to build multi-arch images")
	@Interpolative(variableSuggester="suggestVariables")
	@ReservedOptions({"--push", "-f", "--file", "(-f|--file)=.*", "-t", "--tag", "(-t|--tag)=.*"})
	public String getMoreOptions() {
		return moreOptions;
	}

	public void setMoreOptions(String moreOptions) {
		this.moreOptions = moreOptions;
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
		return new BuildImageFacade(getBuildPath(), getDockerfile(), getTags(), isPublish(), 
				isRemoveDanglingImages(), accessToken, getMoreOptions());
	}
	
}
