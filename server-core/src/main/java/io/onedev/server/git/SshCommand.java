package io.onedev.server.git;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import javax.annotation.Nullable;

import org.apache.shiro.authz.UnauthorizedException;
import org.apache.shiro.util.ThreadContext;
import org.apache.sshd.common.channel.ChannelOutputStream;
import org.apache.sshd.server.Environment;
import org.apache.sshd.server.ExitCallback;
import org.apache.sshd.server.channel.ChannelSession;
import org.apache.sshd.server.command.Command;
import org.apache.sshd.server.session.ServerSession;
import org.apache.sshd.server.session.ServerSessionAware;
import org.eclipse.jgit.transport.RemoteConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;

import io.onedev.commons.utils.StringUtils;
import io.onedev.commons.utils.command.ExecutionResult;
import io.onedev.server.OneDev;
import io.onedev.server.cluster.ClusterManager;
import io.onedev.server.entitymanager.ProjectManager;
import io.onedev.server.git.command.ReceivePackCommand;
import io.onedev.server.git.command.UploadPackCommand;
import io.onedev.server.git.hook.HookUtils;
import io.onedev.server.model.Project;
import io.onedev.server.model.User;
import io.onedev.server.persistence.SessionManager;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.ssh.SshAuthenticator;
import io.onedev.server.ssh.SshManager;
import io.onedev.server.storage.StorageManager;
import io.onedev.server.util.InputStreamWrapper;
import io.onedev.server.util.OutputStreamWrapper;
import io.onedev.server.util.concurrent.PrioritizedRunnable;
import io.onedev.server.util.concurrent.WorkExecutor;
import io.onedev.server.util.facade.ProjectFacade;

class SshCommand implements Command, ServerSessionAware {
	
	private static final int PRIORITY = 2;
	
	private static final int CHANNEL_OPEN_TIMEOUT = 5000;
	
	private static final Logger logger = LoggerFactory.getLogger(SshCommand.class);

	private final String commandString;
	
	private final Map<String, String> environments;
	
	private InputStream in;
	
	private OutputStream out;
	
	private OutputStream err;
	
	private ExitCallback callback;
	
	private ServerSession session;
	
	private Future<?> future;
	
	SshCommand(String commandString, Map<String, String> environments) {
		this.commandString = commandString;
		this.environments = environments;
	}
	
	@Override
	public void start(ChannelSession channel, Environment env) throws IOException {
		boolean upload = commandString.startsWith(RemoteConfig.DEFAULT_UPLOAD_PACK);
		String protocol = environments.get("GIT_PROTOCOL");
		
		SshAuthenticator authenticator = OneDev.getInstance(SshAuthenticator.class);
		ThreadContext.bind(SecurityUtils.asSubject(authenticator.getPublicKeyOwnerId(session)));
		
		boolean clusterAccess = SecurityUtils.getUserId().equals(User.SYSTEM_ID);		
		
		ProjectManager projectManager = OneDev.getInstance(ProjectManager.class);

		String tempStr = StringUtils.substringAfter(commandString, "'/");   
		String projectPath = StringUtils.substringBefore(tempStr, "'");
        ProjectFacade projectFacade = projectManager.findFacadeByPath(projectPath);
		if (projectFacade == null && projectPath.startsWith("projects/")) {
			projectPath = projectPath.substring("projects/".length());
			projectFacade = projectManager.findFacadeByPath(projectPath);
		}
        if (projectFacade == null) {
        	if (clusterAccess || upload) {
	            onExit(-1, "Unable to find project '" + projectPath + "'");
	            return;
        	} else {
        		try {
					Project project = projectManager.setup(projectPath);
					Preconditions.checkState(project.isNew());
					projectManager.create(project);
					projectFacade = project.getFacade();
        		} catch (UnauthorizedException e) {
        			if (e.getMessage() != null)
        				onExit(-1, e.getMessage());
        			else
        				onExit(-1, "Permission denied");
    	            return;
        		}
        	}
        } 
		
		ClusterManager clusterManager = OneDev.getInstance(ClusterManager.class);
		
		UUID storageServerUUID = projectManager.getStorageServerUUID(projectFacade.getId(), true);
		if (clusterAccess || storageServerUUID.equals(clusterManager.getLocalServerUUID())) {
	        File gitDir = OneDev.getInstance(StorageManager.class).getProjectGitDir(projectFacade.getId());
	        Map<String, String> hookEnvs = HookUtils.getHookEnvs(projectFacade.getId(), SecurityUtils.getUserId());

	        if (!clusterAccess) {
		        SessionManager sessionManager = OneDev.getInstance(SessionManager.class);
		        sessionManager.openSession(); 
		        try {
		        	Project project = projectManager.load(projectFacade.getId());
		        	if (upload) {
		    			if (!SecurityUtils.canReadCode(project)) {
		    				onExit(-1, "You are not allowed to pull from the project");
		    				return;
		    			}
		        	} else {
		    			if (!SecurityUtils.canWriteCode(project)) {
		    				onExit(-1, "You are not allowed to push to the project");
		    				return;
		    			}
		        	}
		        } finally {                
		            sessionManager.closeSession();
		        }
	        }
			
	        String groupId = "git-over-ssh-" + projectFacade.getId() + "-" + SecurityUtils.getUserId();
	        
	        WorkExecutor workExecutor = OneDev.getInstance(WorkExecutor.class);
			future = workExecutor.submit(groupId, new PrioritizedRunnable(PRIORITY) {
				
				@Override
				public void run() {
					try {
						ExecutionResult result;
						if (upload) {
							result = new UploadPackCommand(gitDir, in, out, err, hookEnvs)
				            		.protocol(protocol)
				            		.run();
						} else {
							result = new ReceivePackCommand(gitDir, in, out, err, hookEnvs)
				            		.protocol(protocol)
				            		.run();
						}
						onExit(result.getReturnCode(), null);
					} catch (Exception e) {
						logger.error("Error executing git command", e);
						onExit(-1, e.getMessage());
					}
				}
				
			});
		} else {
			ExecutorService executorService = OneDev.getInstance(ExecutorService.class);
			future = executorService.submit(new Runnable() {

				@Override
				public void run() {
					SshManager sshManager = OneDev.getInstance(SshManager.class);
					try (	var clientSession = sshManager.ssh(storageServerUUID); 
							var clientChannel = clientSession.createExecChannel(commandString)) {
						clientChannel.setIn(in);
						clientChannel.setOut(out);
						clientChannel.setErr(err);
						clientChannel.open().await(CHANNEL_OPEN_TIMEOUT);
						
						// Do not use clientChannel.waitFor here as it can not be interrupted
						while (!clientChannel.isClosed())
							Thread.sleep(1000);
						if (clientChannel.getExitStatus() != null)
							onExit(clientChannel.getExitStatus(), null);
						else
							onExit(-1, null);
					} catch (Exception e) {
						logger.error("Error ssh to storage server", e);
						onExit(-1, e.getMessage());
					}
				}
				
			});
		}
	}
	
	@Override
	public void destroy(ChannelSession channel) throws Exception {
		if (future != null) 
			future.cancel(true);
	}

	private void onExit(int exitValue, @Nullable String errorMessage) {
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