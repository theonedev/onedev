package io.onedev.server.git;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Future;

import javax.annotation.Nullable;

import org.apache.shiro.util.ThreadContext;
import org.apache.sshd.common.channel.ChannelOutputStream;
import org.apache.sshd.server.Environment;
import org.apache.sshd.server.ExitCallback;
import org.apache.sshd.server.channel.ChannelSession;
import org.apache.sshd.server.command.Command;
import org.apache.sshd.server.session.ServerSession;
import org.apache.sshd.server.session.ServerSessionAware;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.onedev.commons.utils.StringUtils;
import io.onedev.commons.utils.command.ExecutionResult;
import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.ProjectManager;
import io.onedev.server.entitymanager.SettingManager;
import io.onedev.server.model.Project;
import io.onedev.server.persistence.SessionManager;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.ssh.SshAuthenticator;
import io.onedev.server.util.InputStreamWrapper;
import io.onedev.server.util.OutputStreamWrapper;
import io.onedev.server.util.ServerConfig;
import io.onedev.server.util.concurrent.WorkExecutor;
import io.onedev.server.util.concurrent.PrioritizedRunnable;

abstract class GitSshCommand implements Command, ServerSessionAware {
	
	private static final int PRIORITY = 2;
	
	private static final Logger logger = LoggerFactory.getLogger(GitSshCommand.class);

	final String command;
	
	InputStream in;
	
	OutputStream out;
	
	OutputStream err;
	
	ExitCallback callback;
	
	ServerSession session;
	
	Future<?> future;
	
	GitSshCommand(String command) {
		this.command = command;
	}
	
    private Map<String, String> buildGitEnvs(Project project) {
		Map<String, String> environments = new HashMap<>();
		
		ServerConfig serverConfig = OneDev.getInstance(ServerConfig.class);
		String serverUrl;
        if (serverConfig.getHttpPort() != 0)
            serverUrl = "http://localhost:" + serverConfig.getHttpPort();
        else 
            serverUrl = "https://localhost:" + serverConfig.getHttpsPort();

        SettingManager settingManager = OneDev.getInstance(SettingManager.class);
        environments.put("ONEDEV_CURL", settingManager.getSystemSetting().getCurlConfig().getExecutable());
		environments.put("ONEDEV_URL", serverUrl);
		environments.put("ONEDEV_USER_ID", SecurityUtils.getUserId().toString());
		environments.put("ONEDEV_REPOSITORY_ID", project.getId().toString());
		return environments;
    }
    
	@Override
	public void start(ChannelSession channel, Environment env) throws IOException {
		SshAuthenticator authenticator = OneDev.getInstance(SshAuthenticator.class);
		ThreadContext.bind(SecurityUtils.asSubject(authenticator.getPublicKeyOwnerId(session)));
		
        File gitDir;
        Map<String, String> gitEnvs;
        
        SessionManager sessionManager = OneDev.getInstance(SessionManager.class);
        sessionManager.openSession(); 
        try {
			ProjectManager projectManager = OneDev.getInstance(ProjectManager.class);
			String projectPath = StringUtils.substringAfter(command, "'/");   
			projectPath = StringUtils.substringBefore(projectPath, "'");
            Project project = projectManager.findByPath(projectPath);
    		if (project == null && projectPath.startsWith("projects/")) {
    			projectPath = projectPath.substring("projects/".length());
    			project = projectManager.findByPath(projectPath);
    		}
            if (project == null) {
                onExit(-1, "Unable to find project '" + projectPath + "'");
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
		
        InetSocketAddress address = (InetSocketAddress) session.getRemoteAddress();
        String groupId = "git-over-ssh-" + gitDir.getAbsolutePath() 
        		+ "-" + address.getAddress().getHostAddress();
        
        WorkExecutor workExecutor = OneDev.getInstance(WorkExecutor.class);
		future = workExecutor.submit(groupId, new PrioritizedRunnable(PRIORITY) {
			
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
	public void destroy(ChannelSession channel) throws Exception {
		if (future != null)
			future.cancel(true);
	}

	protected void onExit(int exitValue, @Nullable String errorMessage) {
		if (errorMessage != null)
			new PrintStream(err).println("ERROR: " + errorMessage);
		callback.onExit(exitValue);
	}

	@Override
	public void setInputStream(InputStream in) {
		this.in = new InputStreamWrapper(in) {

			@Override
			public void close() throws IOException {
			}
			
		};
	}

	@Override
	public void setOutputStream(OutputStream out) {
		this.out = new OutputStreamWrapper(out) {
			
			@Override
			public void close() throws IOException {
			}
			
		};
		((ChannelOutputStream) out).setNoDelay(true);
	}

	@Override
	public void setErrorStream(OutputStream err) {
		this.err = new OutputStreamWrapper(err) {
			
			@Override
			public void close() throws IOException {
			}
			
		};
		((ChannelOutputStream) err).setNoDelay(true);
	}

	@Override
	public void setExitCallback(ExitCallback callBack) {
		this.callback = callBack;
	}

	@Override
	public void setSession(ServerSession session) {
		this.session = session;
	}

}