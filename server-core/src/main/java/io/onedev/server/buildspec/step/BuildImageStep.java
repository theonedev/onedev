package io.onedev.server.buildspec.step;

import static io.onedev.server.buildspec.step.StepGroup.DOCKER_IMAGE;
import static java.util.stream.Collectors.toList;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import io.onedev.commons.codeassist.InputSuggestion;
import io.onedev.k8shelper.BuildImageFacade;
import io.onedev.k8shelper.StepFacade;
import io.onedev.server.annotation.Editable;
import io.onedev.server.annotation.Interpolative;
import io.onedev.server.annotation.NoSpace;
import io.onedev.server.annotation.ReservedOptions;
import io.onedev.server.annotation.SubPath;
import io.onedev.server.buildspec.BuildSpec;
import io.onedev.server.buildspec.param.ParamCombination;
import io.onedev.server.model.Build;
import io.onedev.server.model.support.administration.jobexecutor.DockerAware;
import io.onedev.server.model.support.administration.jobexecutor.JobExecutor;
import io.onedev.server.model.support.administration.jobexecutor.KubernetesAware;

@Editable(order=160, name="Build Image", group = DOCKER_IMAGE, description="Build docker image with docker buildx. " +
		"This step can only be executed by server docker executor or remote docker executor, and it uses the buildx " +
		"builder specified in these executors to do the job. To build image with Kubernetes executor, please use kaniko " +
		"step instead")
public class BuildImageStep extends Step {

	private static final long serialVersionUID = 1L;

	private String buildPath;
	
	private String dockerfile;

	private Output output = new RegistryOutput();
	
	private List<RegistryLogin> registryLogins = new ArrayList<>();
	
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

	@Editable(order=1000, group="More Settings", description="Optionally specify registry logins to override " +
			"those defined in job executor. For built-in registry, use <code>@server_url@</code> for registry url, " +
			"<code>@job_token@</code> for user name, and access token secret for password secret")
	@Valid
	public List<RegistryLogin> getRegistryLogins() {
		return registryLogins;
	}

	public void setRegistryLogins(List<RegistryLogin> registryLogins) {
		this.registryLogins = registryLogins;
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
		var registryLogins = getRegistryLogins().stream().map(it->it.getFacade(build)).collect(toList());
		return new BuildImageFacade(getBuildPath(), getDockerfile(), getOutput().getFacade(), 
				registryLogins, getPlatforms(), getMoreOptions());
	}
	
	@Override
	public boolean isApplicable(Build build, JobExecutor executor) {
		return executor instanceof DockerAware && !(executor instanceof KubernetesAware);
	}

	@Editable
	public static interface Output extends Serializable {
		
		BuildImageFacade.Output getFacade();
		
	}
	
	@Editable(order=100, name="Push to container registry")
	public static class RegistryOutput implements Output {

		private static final long serialVersionUID = 1L;

		private String tags;

		@Editable(description="Specify image tags to push, for instance <tt>registry-server:5000/myorg/myrepo:latest</tt>. " +
				"Make sure to use <b>same host</b> as specified in server url of system settings if you want to push to " +
				"built-in registry, or simply use the form <tt>@server@/&lt;project path&gt;/&lt;repo name&gt;:&lt;tag name&gt;</tt>. Multiple " +
				"tags should be separated with space")
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
