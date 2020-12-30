package io.onedev.server.git;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Future;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.shiro.util.ThreadContext;
import org.apache.sshd.common.channel.ChannelOutputStream;
import org.apache.sshd.server.Environment;
import org.apache.sshd.server.ExitCallback;
import org.apache.sshd.server.SessionAware;
import org.apache.sshd.server.command.Command;
import org.apache.sshd.server.session.ServerSession;
import org.eclipse.jgit.transport.RemoteConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.onedev.commons.utils.StringUtils;
import io.onedev.commons.utils.command.ExecutionResult;
import io.onedev.server.entitymanager.ProjectManager;
import io.onedev.server.entitymanager.SettingManager;
import io.onedev.server.git.command.ReceivePackCommand;
import io.onedev.server.git.command.UploadPackCommand;
import io.onedev.server.model.Project;
import io.onedev.server.persistence.SessionManager;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.ssh.SshAuthenticator;
import io.onedev.server.ssh.SshCommandCreator;
import io.onedev.server.util.ServerConfig;
import io.onedev.server.util.concurrent.PrioritizedRunnable;
import io.onedev.server.util.work.WorkExecutor;

@Singleton
public class GitSshCommandCreator implements SshCommandCreator {

	private static final Logger logger = LoggerFactory.getLogger(GitSshCommandCreator.class);

	private final ServerConfig serverConfig;
	
	private final SettingManager settingManager;
	
	private final ProjectManager projectManager;
	
	private final SessionManager sessionManager;
	
	private final SshAuthenticator authenticator;
	
	private final WorkExecutor workExecutor;
	
	@Inject
	public GitSshCommandCreator(ServerConfig serverConfig, SettingManager settingManager, 
			ProjectManager projectManager, SessionManager sessionManager, 
			WorkExecutor workExecutor, SshAuthenticator authenticator) {
		this.serverConfig = serverConfig;
		this.settingManager = settingManager;
		this.projectManager = projectManager;
		this.sessionManager = sessionManager;
		this.workExecutor = workExecutor;
		this.authenticator = authenticator;
	}
	
	@Override
	public Command createCommand(String command) {
		if (command.startsWith(RemoteConfig.DEFAULT_UPLOAD_PACK)) {
			return new GitSshCommand(command) {

				@Override
				protected ExecutionResult execute(File gitDir, Map<String, String> gitEnvs) {
		            return new UploadPackCommand(gitDir, gitEnvs)
		            		.stdin(inputStream)
		            		.stdout(outputStream)
		            		.stderr(errorStream)
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
		            		.stdin(inputStream)
		            		.stdout(outputStream)
		            		.stderr(errorStream)
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
		} else {
			return null;
		}
	}

	private abstract class GitSshCommand implements Command, SessionAware {
		
		private static final int PRIORITY = 2;

		final String command;
		
		InputStream inputStream;
		
		OutputStream outputStream;
		
		OutputStream errorStream;
		
		ExitCallback exitCallBack;
		
		ServerSession session;
		
		Future<?> commandFuture;
		
		GitSshCommand(String command) {
			this.command = command;
		}
		
	    private Map<String, String> buildGitEnvs(Project project) {
			Map<String, String> environments = new HashMap<>();
			
			String serverUrl;
	        if (serverConfig.getHttpPort() != 0)
	            serverUrl = "http://localhost:" + serverConfig.getHttpPort();
	        else 
	            serverUrl = "https://localhost:" + serverConfig.getHttpsPort();

	        environments.put("ONEDEV_CURL", settingManager.getSystemSetting().getCurlConfig().getExecutable());
			environments.put("ONEDEV_URL", serverUrl);
			environments.put("ONEDEV_USER_ID", SecurityUtils.getUserId().toString());
			environments.put("ONEDEV_REPOSITORY_ID", project.getId().toString());
			return environments;
	    }
	    
		@Override
		public void start(Environment env) throws IOException {
			ThreadContext.bind(SecurityUtils.asSubject(authenticator.getPublicKeyOwnerId(session)));
			
            File gitDir;
            Map<String, String> gitEnvs;
            
            sessionManager.openSession(); 
            try {
    			String projectName = StringUtils.stripEnd(StringUtils.substringAfterLast(command, "/"), "'");   
                Project project = projectManager.find(projectName);
                if (project == null) {
                    onExit(-1, "Unable to find project " + projectName);
                    return;
                } 
                
            	String errorMessage = checkPermission(project);
            	if (errorMessage != null) {
            		onExit(-1, errorMessage);
            		return;
            	} 

                gitDir = project.getGitDir();
                gitEnvs = buildGitEnvs(project);
            } finally {                
                sessionManager.closeSession();
            }
			
			commandFuture = workExecutor.submit(new PrioritizedRunnable(PRIORITY) {
				
				@Override
				public void run() {
					try {
						ExecutionResult result = execute(gitDir, gitEnvs);
						onExit(result.getReturnCode(), null);
					} catch (Exception e) {
						logger.error("Error executing git command", e);
						onExit(-1, e.getMessage());
					}
				}
				
			});
			
		}
		
		@Nullable
		protected abstract String checkPermission(Project project);

		protected abstract ExecutionResult execute(File gitDir, Map<String, String> gitEnvs);

		@Override
		public void destroy() throws Exception {
			if (commandFuture != null)
				commandFuture.cancel(true);
		}

		protected void onExit(int exitValue, @Nullable String errorMessage) {
			if (errorMessage != null)
				new PrintStream(errorStream).println("ERROR: " + errorMessage);
			exitCallBack.onExit(exitValue);
		}

		@Override
		public void setInputStream(InputStream inputStream) {
			this.inputStream = inputStream;
		}

		@Override
		public void setOutputStream(OutputStream outputStream) {
			this.outputStream = outputStream;
			((ChannelOutputStream) this.outputStream).setNoDelay(true);
		}

		@Override
		public void setErrorStream(OutputStream errorStream) {
			this.errorStream = errorStream;
			((ChannelOutputStream) this.errorStream).setNoDelay(true);
		}

		@Override
		public void setExitCallback(ExitCallback exitCallBack) {
			this.exitCallBack = exitCallBack;
		}

		@Override
		public void setSession(ServerSession session) {
			this.session = session;
		}

	}
}
