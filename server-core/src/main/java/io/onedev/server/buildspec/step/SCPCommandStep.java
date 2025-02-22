package io.onedev.server.buildspec.step;

import io.onedev.commons.utils.StringUtils;
import io.onedev.server.annotation.ChoiceProvider;
import io.onedev.server.annotation.Editable;
import io.onedev.server.annotation.Interpolative;
import io.onedev.server.buildspec.step.commandinterpreter.DefaultInterpreter;
import io.onedev.server.buildspec.step.commandinterpreter.Interpreter;
import io.onedev.server.model.Build;
import io.onedev.server.model.Project;

import javax.validation.constraints.NotEmpty;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static io.onedev.server.buildspec.step.StepGroup.UTILITIES;

@Editable(order=1100, group = UTILITIES, name="Copy Files with SCP", description = "" +
		"This step can only be executed by a docker aware executor. It runs under <a href='https://docs.onedev.io/concepts#job-workspace' target='_blank'>job workspace</a>")
public class SCPCommandStep extends CommandStep {

	private static final long serialVersionUID = 1L;
	
	private String privateKeySecret;
	
	private String source;
	
	private String target;
	
	private String options;

	@Editable(order=200, description="Specify a <a href='https://docs.onedev.io/tutorials/cicd/job-secrets' target='_blank'>job secret</a> to be used as private key for SSH authentication. " +
			"<b class='text-info'>NOTE:</b> Private key with passphrase is not supported")
	@ChoiceProvider("getPrivateKeySecretChoices")
	@NotEmpty
	public String getPrivateKeySecret() {
		return privateKeySecret;
	}

	public void setPrivateKeySecret(String privateKeySecret) {
		this.privateKeySecret = privateKeySecret;
	}

	@SuppressWarnings("unused")
	private static List<String> getPrivateKeySecretChoices() {
		return Project.get().getHierarchyJobSecrets()
				.stream().map(it->it.getName()).collect(Collectors.toList());
	}

	@Editable(order=300, description = "Specify source param for SCP command, for instance <code>app.tar.gz</code>")
	@Interpolative(variableSuggester="suggestVariables")
	@NotEmpty
	public String getSource() {
		return source;
	}
	
	public void setSource(String source) {
		this.source = source;
	}

	@Editable(order=400, description = "Specify target param for SCP command, for instance <code>user@@host:/app</code>. " +
			"<b class='text-info'>NOTE:</b> Make sure that scp command is installed on remote host")
	@Interpolative(variableSuggester="suggestVariables")
	@NotEmpty
	public String getTarget() {
		return target;
	}

	public void setTarget(String target) {
		this.target = target;
	}

	@Editable(order=500, description = "Optionally specify options for scp command. Multiple options need to be " +
			"separated with space")
	@Interpolative(variableSuggester="suggestVariables")
	public String getOptions() {
		return options;
	}

	public void setOptions(String options) {
		this.options = options;
	}

	@Editable
	@Override
	public boolean isRunInContainer() {
		return true;
	}

	@Editable
	@Override
	public String getImage() {
		return "1dev/ssh-client:1.1.0";
	}

	@Override
	public String getRunAs() {
		return null;
	}

	@Override
	public boolean isUseTTY() {
		return false;
	}

	@Override
	public List<RegistryLogin> getRegistryLogins() {
		return new ArrayList<>();
	}
	
	@Override
	public Interpreter getInterpreter() {
		return new DefaultInterpreter() {
			@Override
			public String getCommands() {
				var commandsBuilder = new StringBuilder();
				commandsBuilder.append("mkdir /root/.ssh\n");
				commandsBuilder.append("cat <<EOF>> /root/.ssh/id_rsa\n");
				var privateKey = Build.get().getJobAuthorizationContext().getSecretValue(getPrivateKeySecret());
				for (var line: StringUtils.splitToLines(privateKey))
					commandsBuilder.append(line).append("\n");
				var scpBuilder = new StringBuilder("scp -o StrictHostKeyChecking=no ");
				if (getOptions() != null)
					scpBuilder.append(getOptions()).append(" ");
				scpBuilder.append(getSource()).append(" ").append(getTarget());
				commandsBuilder.append("EOF\n");
				commandsBuilder.append("chmod 600 /root/.ssh/id_rsa\n");
				commandsBuilder.append(scpBuilder).append("\n");
				return commandsBuilder.toString();
			}
		};
	}
	
}
