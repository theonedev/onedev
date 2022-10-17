package io.onedev.server.git;

import java.util.Map;

import javax.inject.Singleton;

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
		if (commandString.startsWith(RemoteConfig.DEFAULT_UPLOAD_PACK) 
				|| commandString.startsWith(RemoteConfig.DEFAULT_RECEIVE_PACK)) {
			return new SshCommand(commandString, environments);
		} else if (commandString.startsWith(LfsAuthenticateCommand.COMMAND_PREFIX)) {
			return new LfsAuthenticateCommand(commandString);
		} else {
			return null;
		}
	}
	
}
