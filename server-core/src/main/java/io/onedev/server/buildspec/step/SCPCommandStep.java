package io.onedev.server.buildspec.step;

import io.onedev.commons.utils.StringUtils;
import io.onedev.server.annotation.ChoiceProvider;
import io.onedev.server.annotation.Editable;
import io.onedev.server.annotation.Interpolative;
import io.onedev.server.annotation.ReservedOptions;
import io.onedev.server.buildspec.step.commandinterpreter.DefaultInterpreter;
import io.onedev.server.buildspec.step.commandinterpreter.Interpreter;
import io.onedev.server.model.Build;
import io.onedev.server.model.Project;

import javax.validation.constraints.NotEmpty;
import java.util.List;
import java.util.stream.Collectors;

import static com.google.common.collect.Lists.newArrayList;

@Editable(order=135, name="Copy Files with SCP", description = "" +
		"This step can only be executed by a docker aware executor. It runs under <a href='https://docs.onedev.io/concepts#job-workspace' target='_blank'>job workspace</a>")
public class SCPCommandStep extends CommandStep {

	private static final long serialVersionUID = 1L;
	
	private String privateKeySecret;
	
	private String source;
	
	private String target;
	
	private String options;

	@Editable(order=200, description="Specify a secret to be used as private key for SSH authentication")
	@ChoiceProvider("getPrivateKeySecretChoices")
	@NotEmpty
	public String getPrivateKeySecret() {
		return privateKeySecret;
	}

	public void setPrivateKeySecret(String privateKeySecret) {
		this.privateKeySecret = privateKeySecret;
	}

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

	@Editable(order=400, description = "Specify target param for SCP command, for instance <code>user@host:/app</code>")
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
	public boolean isUseTTY() {
		return false;
	}

	@Override
	public Interpreter getInterpreter() {
		return new DefaultInterpreter() {
			@Override
			public List<String> getCommands() {
				var commands = newArrayList(
						"mkdir /root/.ssh",
						"cat <<EOF>> /root/.ssh/id_rsa");
				var privateKey = Build.get().getJobSecretAuthorizationContext().getSecretValue(getPrivateKeySecret());
				commands.addAll(StringUtils.splitToLines(privateKey));
				var scpBuilder = new StringBuilder("scp -o StrictHostKeyChecking=no ");
				if (getOptions() != null)
					scpBuilder.append(getOptions()).append(" ");
				scpBuilder.append(getSource()).append(" ").append(getTarget());
				commands.addAll(newArrayList(
						"EOF",
						"chmod 600 /root/.ssh/id_rsa",
						scpBuilder.toString()));
				return commands;
			}
		};
	}
	
}
