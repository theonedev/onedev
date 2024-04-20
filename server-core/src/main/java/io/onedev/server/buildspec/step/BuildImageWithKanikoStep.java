package io.onedev.server.buildspec.step;

import io.onedev.agent.BuiltInRegistryLogin;
import io.onedev.commons.codeassist.InputSuggestion;
import io.onedev.commons.utils.StringUtils;
import io.onedev.k8shelper.CommandFacade;
import io.onedev.server.OneDev;
import io.onedev.server.annotation.*;
import io.onedev.server.buildspec.BuildSpec;
import io.onedev.server.buildspec.step.commandinterpreter.DefaultInterpreter;
import io.onedev.server.buildspec.step.commandinterpreter.Interpreter;
import io.onedev.server.entitymanager.SettingManager;
import io.onedev.server.model.support.administration.jobexecutor.JobExecutor;
import io.onedev.server.model.support.administration.jobexecutor.RegistryLogin;
import io.onedev.server.model.support.administration.jobexecutor.RegistryLoginAware;
import io.onedev.server.util.UrlUtils;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import static io.onedev.agent.DockerExecutorUtils.buildDockerConfig;
import static io.onedev.server.buildspec.step.StepGroup.DOCKER_IMAGE;
import static java.util.stream.Collectors.toList;

@Editable(order=200, name="Build Docker Image (Kaniko)", group = DOCKER_IMAGE, description="Build docker image with kaniko. " +
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
		return "1dev/kaniko:1.0.2";
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

	@Editable(order=1100, name="Built-in Registry Access Token Secret", group="More Settings", descriptionProvider = "getBuiltInRegistryAccessTokenSecretDescription")
	@ChoiceProvider("getAccessTokenSecretChoices")
	@Override
	public String getBuiltInRegistryAccessTokenSecret() {
		return super.getBuiltInRegistryAccessTokenSecret();
	}

	@Override
	public void setBuiltInRegistryAccessTokenSecret(String builtInRegistryAccessTokenSecret) {
		super.setBuiltInRegistryAccessTokenSecret(builtInRegistryAccessTokenSecret);
	}

	private static String getBuiltInRegistryAccessTokenSecretDescription() {
		var serverUrl = OneDev.getInstance(SettingManager.class).getSystemSetting().getServerUrl();
		var server = UrlUtils.getServer(serverUrl);
		return "Optionally specify a secret to be used as access token for built-in registry server " +
				"<code>" + server + "</code>";
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
											   String builtInRegistryAccessToken, boolean useTTY) {
				var commandsBuilder = new StringBuilder();
				if (jobExecutor instanceof RegistryLoginAware) {
					RegistryLoginAware registryLoginAware = (RegistryLoginAware) jobExecutor;
					commandsBuilder.append("cat <<EOF>> /kaniko/.docker/config.json\n");
					var registryLogins = registryLoginAware.getRegistryLogins().stream().map(RegistryLogin::getFacade).collect(toList());
					var builtInRegistryUrl = OneDev.getInstance(SettingManager.class).getSystemSetting().getServerUrl();
					var builtInRegistryLogin = new BuiltInRegistryLogin(builtInRegistryUrl, jobToken, builtInRegistryAccessToken);
					commandsBuilder.append(buildDockerConfig(registryLogins, builtInRegistryLogin)).append("\n");
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
				return new CommandFacade(image, runAs, builtInRegistryAccessToken, commandsBuilder.toString(), useTTY);
			}
			
		};
	}

	@Editable
	public static interface Output extends Serializable {
		
		String getOptions();
		
	}

	@Editable(order=100, name="Push to Container registry")
	public static class RegistryOutput implements Output {

		private static final long serialVersionUID = 1L;
		
		private String destinations;

		@Editable(order=300, description="Specify destinations, for instance <tt>myorg/myrepo:latest</tt>, "
				+ "<tt>myorg/myrepo:1.0.0</tt>, or <tt>myregistry:5000/myorg/myrepo:1.0.0</tt>. "
				+ "Multiple destinations should be separated with space.<br>")
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
			for (var destination: StringUtils.splitAndTrim(getDestinations(), " "))
				options.add("--destination=" + destination);
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
