package io.onedev.server.git;

import java.io.File;
import java.util.Map;

import javax.inject.Singleton;

import org.apache.sshd.server.command.Command;
import org.eclipse.jgit.transport.RemoteConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.onedev.commons.utils.command.ExecutionResult;
import io.onedev.server.git.command.ReceivePackCommand;
import io.onedev.server.git.command.UploadPackCommand;
import io.onedev.server.model.Project;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.ssh.SshCommandCreator;

@Singleton
public class GitSshCommandCreator implements SshCommandCreator {

	static final Logger logger = LoggerFactory.getLogger(GitSshCommandCreator.class);
	
	@Override
	public Command createCommand(String command) {
		if (command.startsWith(RemoteConfig.DEFAULT_UPLOAD_PACK)) {
			return new GitSshCommand(command) {

				@Override
				protected ExecutionResult execute(File gitDir, Map<String, String> gitEnvs) {
		            return new UploadPackCommand(gitDir, gitEnvs)
		            		.stdin(in)
		            		.stdout(out)
		            		.stderr(err)
		            		.call();
		        }

				@Override
				protected String checkPermission(Project project) {
					if (!SecurityUtils.canReadCode(project))
						return "You are not allowed to pull from the project";
					else
						return null;
				}			
				
			};
		} else if (command.startsWith(RemoteConfig.DEFAULT_RECEIVE_PACK)) {
			return new GitSshCommand(command) {

				@Override
				protected ExecutionResult execute(File gitDir, Map<String, String> gitEnvs) {
		            return new ReceivePackCommand(gitDir, gitEnvs)
		            		.stdin(in)
		            		.stdout(out)
		            		.stderr(err)
		            		.call();
				}

				@Override
				protected String checkPermission(Project project) {
					if (!SecurityUtils.canWriteCode(project))
						return "You are not allowed to push to the project";
					else
						return null;
				}
				
			};
		} else if (command.startsWith(LfsAuthenticateCommand.COMMAND_PREFIX)) {
			return new LfsAuthenticateCommand(command);
		} else {
			return null;
		}
	}
	
}
