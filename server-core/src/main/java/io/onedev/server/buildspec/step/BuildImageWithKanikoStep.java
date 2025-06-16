package io.onedev.server.buildspec.step;

import io.onedev.commons.codeassist.InputSuggestion;
import io.onedev.commons.utils.ExplicitException;
import io.onedev.commons.utils.StringUtils;
import io.onedev.k8shelper.CommandFacade;
import io.onedev.k8shelper.RegistryLoginFacade;
import io.onedev.server.annotation.*;
import io.onedev.server.buildspec.BuildSpec;
import io.onedev.server.buildspec.step.commandinterpreter.DefaultInterpreter;
import io.onedev.server.buildspec.step.commandinterpreter.Interpreter;
import io.onedev.server.model.support.administration.jobexecutor.JobExecutor;
import io.onedev.server.model.support.administration.jobexecutor.DockerAware;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static io.onedev.agent.DockerExecutorUtils.buildDockerConfig;
import static io.onedev.k8shelper.RegistryLoginFacade.merge;
import static io.onedev.server.buildspec.step.StepGroup.DOCKER_IMAGE;

@Editable(order=200, name="Build Image (Kaniko)", group = DOCKER_IMAGE, description="Build docker image with kaniko. " +
		"This step needs to be executed by server docker executor, remote docker executor, or Kubernetes executor")
public class BuildImageWithKanikoStep extends CommandStep {

	private static final long serialVersionUID = 1L;

	private String buildContext;
	
	private Output output = new RegistryOutput();
	
	private String trustCertificates;
	
	private String moreOptions;

	@Editable
	@Override
	public boolean isRunInContainer() {
		return true;
	}

	@Editable
	@Override
	public String getImage() {
		return "1dev/kaniko:1.0.3";
	}

	@Override
	public boolean isUseTTY() {
		return true;
	}

	@Override
	public String getRunAs() {
		return null;
	}

	@Editable(order=100, description="Optionally specify build context path relative to <a href='https://docs.onedev.io/concepts#job-workspace' target='_blank'>job workspace</a>. "
			+ "Leave empty to use job workspace itself. The file <code>Dockerfile</code> is expected to exist in build context " +
			"directory, unless you specify a different location with option <code>--dockerfile</code>")
	@Interpolative(variableSuggester="suggestVariables")
	@SubPath
	public String getBuildContext() {
		return buildContext;
	}

	public void setBuildContext(String buildContext) {
		this.buildContext = buildContext;
	}

	@Editable(order=300)
	@NotNull
	public Output getOutput() {
		return output;
	}

	public void setOutput(Output output) {
		this.output = output;
	}

	@Editable(order=1000, name="Certificates to Trust", group = "More Settings", placeholder = "Base64 encoded PEM format, starting with " +
			"-----BEGIN CERTIFICATE----- and ending with -----END CERTIFICATE-----",
			description = "Specify certificates to trust if you are using self-signed certificates for your docker registries")
	@Multiline(monospace = true)
	@Interpolative(variableSuggester="suggestVariables")
	public String getTrustCertificates() {
		return trustCertificates;
	}

	public void setTrustCertificates(String trustCertificates) {
		this.trustCertificates = trustCertificates;
	}

	@Editable(order=1100, group="More Settings", description="Optionally specify registry logins to override " +
			"those defined in job executor. For built-in registry, use <code>@server_url@</code> for registry url, " +
			"<code>@job_token@</code> for user name, and access token secret for password secret")
	@Override
	public List<RegistryLogin> getRegistryLogins() {
		return super.getRegistryLogins();
	}

	@Override
	public void setRegistryLogins(List<RegistryLogin> registryLogins) {
		super.setRegistryLogins(registryLogins);
	}

	@Editable(order=1200, group="More Settings", description="Optionally specify <a href='https://github.com/GoogleContainerTools/kaniko?tab=readme-ov-file#additional-flags' target='_blank'>additional options</a> of kaniko")
	@Interpolative(variableSuggester="suggestVariables")
	@ReservedOptions({"(--context)=.*", "(--destination)=.*", "(--oci-layout-path)=.*", "--no-push"})
	public String getMoreOptions() {
		return moreOptions;
	}

	public void setMoreOptions(String moreOptions) {
		this.moreOptions = moreOptions;
	}

	@Override
	public Interpreter getInterpreter() {
		return new DefaultInterpreter() {
			
			@Override
			public CommandFacade getExecutable(JobExecutor jobExecutor, String jobToken, String image, String runAs,
											   List<RegistryLoginFacade> registryLogins, Map<String, String> envMap, 
											   boolean useTTY) {
				var commandsBuilder = new StringBuilder();
				if (jobExecutor instanceof DockerAware) {
					DockerAware registryLoginAware = (DockerAware) jobExecutor;
					commandsBuilder.append("cat <<EOF>> /kaniko/.docker/config.json\n");
					var mergedRegistryLogins = merge(registryLogins, registryLoginAware.getRegistryLogins(jobToken));
					commandsBuilder.append(buildDockerConfig(mergedRegistryLogins)).append("\n");
					commandsBuilder.append("EOF\n");
				}
				if (getTrustCertificates() != null) {
					commandsBuilder.append("cat <<EOF>> /kaniko/ssl/certs/additional-ca-cert-bundle.crt\n");
					commandsBuilder.append(getTrustCertificates().replace("\r\n", "\n")).append("\n");
					commandsBuilder.append("EOF\n");
				}
				
				commandsBuilder.append("/kaniko/executor");
				if (getBuildContext() != null)
					commandsBuilder.append(" --context=\"/onedev-build/workspace/").append(getBuildContext()).append("\"");
				else
					commandsBuilder.append(" --context=/onedev-build/workspace");

				commandsBuilder.append(" ").append(getOutput().getOptions());			
				
				if (getMoreOptions() != null)
					commandsBuilder.append(" ").append(getMoreOptions());
				
				commandsBuilder.append("\n");
				
				return new CommandFacade(image, runAs, registryLogins, commandsBuilder.toString(), envMap, useTTY);
			}
			
		};
	}

	@Editable
	public static interface Output extends Serializable {
		
		String getOptions();
		
	}

	@Editable(order=100, name="Push to container registry")
	public static class RegistryOutput implements Output {

		private static final long serialVersionUID = 1L;
		
		private String destinations;

		@Editable(order=300, description="Specify destinations, for instance <tt>registry-server:5000/myorg/myrepo:latest</tt>. " +
				"Make sure to use <b>same host</b> as specified in server url of system settings if you want to push to " +
				"built-in registry, or simply use the form <tt>@server@/&lt;project path&gt;/&lt;repo name&gt;:&lt;tag name&gt;</tt>. Multiple " +
				"destinations should be separated with space")
		@Interpolative(variableSuggester="suggestVariables")
		@NotEmpty
		public String getDestinations() {
			return destinations;
		}

		public void setDestinations(String destinations) {
			this.destinations = destinations;
		}

		static List<InputSuggestion> suggestVariables(String matchWith) {
			return BuildSpec.suggestVariables(matchWith, true, true, false);
		}

		@Override
		public String getOptions() {
			var options = new ArrayList<String>();
			for (var destination: StringUtils.splitAndTrim(getDestinations(), " ")) {
				if (destination.contains("localhost") || destination.contains("127.0.0.1"))
					throw new ExplicitException("Loopback address not allowed for destination of Kaniko image build step, please use ip address or host name instead");
				options.add("--destination=" + destination);
			}
			return StringUtils.join(options, " ");
		}
		
	}
	
	@Editable(order=200, name="Export as OCI layout")
	public static class OCIOutput implements Output {

		private static final long serialVersionUID = 1L;

		private String destPath;

		@Editable(name="OCI Layout Directory", description = "Specify relative path under <a href='https://docs.onedev.io/concepts#job-workspace' target='_blank'>job workspace</a> to store OCI layout")
		@Interpolative(variableSuggester="suggestVariables")
		@SubPath
		@NoSpace
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
		public String getOptions() {
			return "--no-push --oci-layout-path /onedev-build/workspace/" + getDestPath();
		}
		
	}
	
}
