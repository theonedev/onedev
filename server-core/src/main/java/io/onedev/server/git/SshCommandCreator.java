package io.onedev.server.git;

import java.util.Map;

import jakarta.inject.Singleton;

import org.apache.sshd.server.command.Command;
import org.eclipse.jgit.transport.RemoteConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.onedev.server.ssh.CommandCreator;

@Singleton
public class SshCommandCreator implements CommandCreator {

	static final Logger logger = LoggerFactory.getLogger(SshCommandCreator.class);
	
	@Override
	public Command createCommand(String commandString, Map<String, String> environments) {
		boolean forwarded = commandString.startsWith(SshCommand.FORWARD_PREFIX);
		String gitCommand = forwarded ? commandString.substring(SshCommand.FORWARD_PREFIX.length()) : commandString;
		if (gitCommand.startsWith(RemoteConfig.DEFAULT_UPLOAD_PACK)
				|| gitCommand.startsWith(RemoteConfig.DEFAULT_RECEIVE_PACK)) {
			return new SshCommand(gitCommand, environments, forwarded);
		} else if (!forwarded && commandString.startsWith(LfsAuthenticateCommand.COMMAND_PREFIX)) {
			return new LfsAuthenticateCommand(commandString);
		} else {
			return null;
		}
	}
	
}
