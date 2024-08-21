package io.onedev.server.buildspec.step;

import io.onedev.agent.BuiltInRegistryLogin;
import io.onedev.k8shelper.CommandFacade;
import io.onedev.server.OneDev;
import io.onedev.server.annotation.ChoiceProvider;
import io.onedev.server.annotation.Editable;
import io.onedev.server.annotation.Interpolative;
import io.onedev.server.annotation.Multiline;
import io.onedev.server.buildspec.job.EnvVar;
import io.onedev.server.buildspec.step.commandinterpreter.DefaultInterpreter;
import io.onedev.server.buildspec.step.commandinterpreter.Interpreter;
import io.onedev.server.entitymanager.SettingManager;
import io.onedev.server.model.support.administration.jobexecutor.JobExecutor;
import io.onedev.server.model.support.administration.jobexecutor.RegistryLogin;
import io.onedev.server.model.support.administration.jobexecutor.RegistryLoginAware;
import io.onedev.server.util.UrlUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static io.onedev.agent.DockerExecutorUtils.buildDockerConfig;
import static java.util.stream.Collectors.toList;

public abstract class CraneStep extends CommandStep {

	private static final long serialVersionUID = 1L;
	
	private String trustCertificates;

	private List<EnvVar> envVars = new ArrayList<>();
	
	@Editable
	@Override
	public boolean isRunInContainer() {
		return true;
	}

	@Editable
	@Override
	public String getImage() {
		return "1dev/crane:1.0.0";
	}

	@Override
	public boolean isUseTTY() {
		return true;
	}

	@Override
	public String getRunAs() {
		return null;
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
		return "Optionally specify a <a href='https://docs.onedev.io/tutorials/cicd/job-secrets' target='_blank'>job secret</a> to be used as access token for built-in registry server " +
				"<code>" + server + "</code>";
	}

	@Editable(order=1150, name="Environment Variables", group="More Settings", description="Optionally specify environment "
			+ "variables for this step")
	public List<EnvVar> getEnvVars() {
		return envVars;
	}

	public void setEnvVars(List<EnvVar> envVars) {
		this.envVars = envVars;
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
					commandsBuilder.append("mkdir /root/.docker\n");
					commandsBuilder.append("cat <<EOF>> /root/.docker/config.json\n");
					var registryLogins = registryLoginAware.getRegistryLogins().stream().map(RegistryLogin::getFacade).collect(toList());
					var builtInRegistryUrl = OneDev.getInstance(SettingManager.class).getSystemSetting().getServerUrl();
					var builtInRegistryLogin = new BuiltInRegistryLogin(builtInRegistryUrl, jobToken, builtInRegistryAccessToken);
					commandsBuilder.append(buildDockerConfig(registryLogins, builtInRegistryLogin)).append("\n");
					commandsBuilder.append("EOF\n");
				}
				if (getTrustCertificates() != null) {
					commandsBuilder.append("cat <<EOF>> /root/trust-certs.crt\n");
					commandsBuilder.append(getTrustCertificates().replace("\r\n", "\n")).append("\n");
					commandsBuilder.append("EOF\n");
					commandsBuilder.append("export SSL_CERT_FILE=/root/trust-certs.crt");
				}
				commandsBuilder.append(getCommand());
				var envMap = new HashMap<String, String>();
				for (var envVar: getEnvVars())
					envMap.put(envVar.getName(), envVar.getValue());
				return new CommandFacade(image, runAs, builtInRegistryAccessToken, commandsBuilder.toString(), 
						envMap, useTTY);
			}
			
		};
	}
	
	protected abstract String getCommand();
	
}
