package io.onedev.server.buildspec.step;

import io.onedev.k8shelper.CommandFacade;
import io.onedev.k8shelper.RegistryLoginFacade;
import io.onedev.server.annotation.Editable;
import io.onedev.server.annotation.Interpolative;
import io.onedev.server.annotation.Multiline;
import io.onedev.server.buildspec.step.commandinterpreter.DefaultInterpreter;
import io.onedev.server.buildspec.step.commandinterpreter.Interpreter;
import io.onedev.server.model.support.administration.jobexecutor.JobExecutor;
import io.onedev.server.model.support.administration.jobexecutor.DockerAware;

import java.util.List;
import java.util.Map;

import static io.onedev.agent.DockerExecutorUtils.buildDockerConfig;
import static io.onedev.k8shelper.RegistryLoginFacade.merge;

public abstract class CraneStep extends CommandStep {

	private static final long serialVersionUID = 1L;
	
	private String trustCertificates;
	
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

	@Override
	public Interpreter getInterpreter() {
		return new DefaultInterpreter() {
			
			@Override
			public CommandFacade getExecutable(JobExecutor jobExecutor, String jobToken, String image, String runAs,
											   List<RegistryLoginFacade> registryLogins, Map<String, String> envMap, boolean useTTY) {
				var commandsBuilder = new StringBuilder();
				if (jobExecutor instanceof DockerAware) {
					DockerAware registryLoginAware = (DockerAware) jobExecutor;
					commandsBuilder.append("mkdir /root/.docker\n");
					commandsBuilder.append("cat <<EOF>> /root/.docker/config.json\n");
					var mergedRegistryLogins = merge(registryLogins, registryLoginAware.getRegistryLogins(jobToken));
					commandsBuilder.append(buildDockerConfig(mergedRegistryLogins)).append("\n");
					commandsBuilder.append("EOF\n");
				}
				if (getTrustCertificates() != null) {
					commandsBuilder.append("cat <<EOF>> /root/trust-certs.crt\n");
					commandsBuilder.append(getTrustCertificates().replace("\r\n", "\n")).append("\n");
					commandsBuilder.append("EOF\n");
					commandsBuilder.append("export SSL_CERT_FILE=/root/trust-certs.crt");
				}
				commandsBuilder.append(getCommand());
				return new CommandFacade(image, runAs, registryLogins, commandsBuilder.toString(), 
						envMap, useTTY);
			}
			
		};
	}
	
	protected abstract String getCommand();
	
}
