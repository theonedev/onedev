package io.onedev.server.buildspec.step;

import io.onedev.commons.utils.StringUtils;
import io.onedev.server.annotation.ChoiceProvider;
import io.onedev.server.annotation.Code;
import io.onedev.server.annotation.Editable;
import io.onedev.server.annotation.Interpolative;
import io.onedev.server.buildspec.step.commandinterpreter.DefaultInterpreter;
import io.onedev.server.buildspec.step.commandinterpreter.Interpreter;
import io.onedev.server.model.Build;
import io.onedev.server.model.Project;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;
import java.util.List;
import java.util.stream.Collectors;

import static com.google.common.collect.Lists.newArrayList;

@Editable(order=125, name="Execute Commands via SSH", description = "" +
		"This step can only be executed by a docker aware executor")
public class SSHCommandStep extends CommandStep {

	private static final long serialVersionUID = 1L;
	
	private String remoteMachine;
	
	private String userName;
	
	private String privateKeySecret;
	
	private String options;
	
	private List<String> commands;

	@Editable(order=100, description = "Host name or ip address of remote machine to run commands via SSH")
	@Interpolative(variableSuggester="suggestVariables")
	@NotEmpty
	public String getRemoteMachine() {
		return remoteMachine;
	}

	public void setRemoteMachine(String remoteMachine) {
		this.remoteMachine = remoteMachine;
	}

	@Editable(order=150, description = "Specify user name of above machine for SSH authentication")
	@Interpolative(variableSuggester="suggestVariables")
	@NotEmpty
	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	@Editable(order=200, description="Specify a secret to be used as private key of above user " +
			"for SSH authentication")
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

	@Editable(order=250, description = "Optionally specify options for ssh command. Multiple options need to be " +
			"separated with space")
	@Interpolative(variableSuggester="suggestVariables")
	public String getOptions() {
		return options;
	}

	public void setOptions(String options) {
		this.options = options;
	}
	
	@Editable(order=300, description="Specify commands to be executed on remote machine. " +
			"<b class='text-warning'>Note:</b> user environments will not be picked up when execute these " +
			"commands, set up them explicitly in commands if necessary")
	@Interpolative
	@Code(language=Code.SHELL, variableProvider="suggestVariables")
	@Size(min=1, message="may not be empty")
	public List<String> getCommands() {
		return commands;
	}

	public void setCommands(List<String> commands) {
		this.commands = commands;
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
				
				var sshBuilder = new StringBuilder("ssh -o StrictHostKeyChecking=no ");
				if (getOptions() != null)
					sshBuilder.append(getOptions()).append(" ");
				sshBuilder.append(getUserName() + "@" + getRemoteMachine() + " << EOF");
				commands.addAll(newArrayList(
						"EOF",
						"chmod 600 /root/.ssh/id_rsa",
						sshBuilder.toString()));
				// Fix issue #1456
				for (var command: SSHCommandStep.this.getCommands()) 
					commands.add(command.replace("$", "\\$"));					
				commands.add("EOF");
				return commands;
			}
		};
	}
	
}
