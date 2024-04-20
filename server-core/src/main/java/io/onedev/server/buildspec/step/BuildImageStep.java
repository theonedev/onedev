package io.onedev.server.buildspec.step;

import io.onedev.commons.codeassist.InputSuggestion;
import io.onedev.k8shelper.BuildImageFacade;
import io.onedev.k8shelper.StepFacade;
import io.onedev.server.OneDev;
import io.onedev.server.annotation.*;
import io.onedev.server.buildspec.BuildSpec;
import io.onedev.server.buildspec.param.ParamCombination;
import io.onedev.server.entitymanager.SettingManager;
import io.onedev.server.model.Build;
import io.onedev.server.model.Project;
import io.onedev.server.model.support.administration.jobexecutor.JobExecutor;
import io.onedev.server.util.UrlUtils;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.List;

import static io.onedev.server.buildspec.step.StepGroup.DOCKER_IMAGE;
import static java.util.stream.Collectors.toList;

@Editable(order=160, name="Build Docker Image", group = DOCKER_IMAGE, description="Build docker image with docker buildx. " +
		"This step can only be executed by server docker executor or remote docker executor, and it uses the buildx " +
		"builder specified in these executors to do the job. To build image with Kubernetes executor, please use kaniko " +
		"step instead")
public class BuildImageStep extends Step {

	private static final long serialVersionUID = 1L;

	private String buildPath;
	
	private String dockerfile;

	private Output output = new RegistryOutput();
	
	private String builtInRegistryAccessTokenSecret;
	
	private String platforms;
	
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

	@Editable(order=300)
	@NotNull
	public Output getOutput() {
		return output;
	}

	public void setOutput(Output output) {
		this.output = output;
	}

	@Editable(order=1000, name="Built-in Registry Access Token Secret", group = "More Settings", descriptionProvider = "getBuiltInRegistryAccessTokenSecretDescription")
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

	@Editable(order=1300, group = "More Settings", placeholder = "Current platform", description = "Optionally specify " +
			"<span class='text-info'>comma separated</span> platforms to build, for instance <tt>linux/amd64,linux/arm64</tt>. " +
			"Leave empty to build for platform of the node running the job")
	@Interpolative(variableSuggester="suggestVariables")
	@NoSpace
	public String getPlatforms() {
		return platforms;
	}

	public void setPlatforms(String platforms) {
		this.platforms = platforms;
	}

	@Editable(order=1400, group = "More Settings", description="Optionally specify additional options for " +
			"buildx build command")
	@Interpolative(variableSuggester="suggestVariables")
	@ReservedOptions({"--builder", "(--builder)=.*", "--platform", "(--platform)=.*", "--push", "-f", "--file", "(-f|--file)=.*", "-t", "--tag", "(-t|--tag)=.*", "-o", "--output", "(-o|--output)=.*"})
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
		return new BuildImageFacade(getBuildPath(), getDockerfile(), getOutput().getFacade(), 
				accessToken, getPlatforms(), getMoreOptions());
	}
	
	@Editable
	public static interface Output extends Serializable {
		
		BuildImageFacade.Output getFacade();
		
	}
	
	@Editable(order=100, name="Push to Container registry")
	public static class RegistryOutput implements Output {

		private static final long serialVersionUID = 1L;

		private String tags;

		@Editable(description="Specify full tag of the image, for instance <tt>myorg/myrepo:latest</tt>, " +
				"<tt>myorg/myrepo:1.0.0</tt>, or <tt>onedev.example.com/myproject/myrepo:1.0.0</tt>. " +
				"Multiple tags should be separated with space")
		@Interpolative(variableSuggester="suggestVariables")
		@NotEmpty
		public String getTags() {
			return tags;
		}

		public void setTags(String tags) {
			this.tags = tags;
		}

		static List<InputSuggestion> suggestVariables(String matchWith) {
			return BuildSpec.suggestVariables(matchWith, true, true, false);
		}

		@Override
		public BuildImageFacade.Output getFacade() {
			return new BuildImageFacade.RegistryOutput(getTags());
		}
	}
	
	@Editable(order=200, name="Export as OCI layout")
	public static class OCIOutput implements Output {

		private static final long serialVersionUID = 1L;
		
		private String destPath;

		@Editable(name="OCI Layout Directory", description = "Specify relative path under <a href='https://docs.onedev.io/concepts#job-workspace' target='_blank'>job workspace</a> to store OCI layout")
		@SubPath
		@NoSpace
		@Interpolative(variableSuggester="suggestVariables")
		@NotEmpty
		public String getDestPath() {
			return destPath;
		}

		public void setDestPath(String destPath) {
			this.destPath = destPath;
		}

		static List<InputSuggestion> suggestVariables(String matchWith) {
			return BuildSpec.suggestVariables(matchWith, true, true, false);
		}
		
		@Override
		public BuildImageFacade.Output getFacade() {
			return new BuildImageFacade.OCIOutput(destPath);
		}

	}
	
}
