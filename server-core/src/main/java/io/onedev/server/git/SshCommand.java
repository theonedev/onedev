package io.onedev.server.git;

import static io.onedev.server.model.Project.decodeFullRepoNameAsPath;
import static io.onedev.server.security.SecurityUtils.asPrincipals;
import static io.onedev.server.security.SecurityUtils.asSubject;
import static io.onedev.server.security.SecurityUtils.asUserPrincipal;
import static org.apache.commons.lang3.StringUtils.substringBefore;
import static org.apache.commons.lang3.math.NumberUtils.toLong;

import java.io.File;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import org.jspecify.annotations.Nullable;

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

import io.onedev.commons.utils.ExplicitException;
import io.onedev.commons.utils.StringUtils;
import io.onedev.commons.utils.command.ExecutionResult;
import io.onedev.server.OneDev;
import io.onedev.server.cluster.ClusterService;
import io.onedev.server.service.ProjectService;
import io.onedev.server.git.command.ReceivePackCommand;
import io.onedev.server.git.command.UploadPackCommand;
import io.onedev.server.git.hook.HookUtils;
import io.onedev.server.model.Project;
import io.onedev.server.model.User;
import io.onedev.server.persistence.SessionService;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.ssh.SshAuthenticator;
import io.onedev.server.ssh.SshService;
import io.onedev.server.util.OutputStreamWrapper;
import io.onedev.server.util.concurrent.WorkExecutionService;

class SshCommand implements Command, ServerSessionAware {

	// Older nodes reject this command instead of executing it as the system user.
	static final String FORWARD_PREFIX = "onedev-git-forward ";

	private static final String FORWARDED_USER_ID = "ONEDEV_SSH_USER_ID";
	
	private static final int PACK_PRIORITY = 2;
	
	private static final int CHANNEL_OPEN_TIMEOUT = 5000;
	
	private static final Logger logger = LoggerFactory.getLogger(SshCommand.class);

	private final String commandString;
	
	private final Map<String, String> environments;

	private final boolean forwarded;
	
	private InputStream in;
	
	private OutputStream out;
	
	private OutputStream err;
	
	private ExitCallback callback;
	
	private ServerSession session;
	
	private Future<?> future;
	
	SshCommand(String commandString, Map<String, String> environments, boolean forwarded) {
		this.commandString = commandString;
		this.environments = environments;
		this.forwarded = forwarded;
	}

	private void reportProjectNotFoundOrInaccessible(String projectPath) {
		onExit(-1, "Project not found or inaccessible: " + projectPath);
	}
	
	@Override
	public void start(ChannelSession channel, Environment env) throws IOException {
		boolean upload = commandString.startsWith(RemoteConfig.DEFAULT_UPLOAD_PACK);
		String protocol = environments.get("GIT_PROTOCOL");
		
		SshAuthenticator authenticator = OneDev.getInstance(SshAuthenticator.class);
		Long keyOwnerId = authenticator.getPublicKeyOwnerId(session);
		// Only a cluster connection may supply a forwarded identity, and it must supply one.
		if (forwarded != User.SYSTEM_ID.equals(keyOwnerId)) {
			onExit(-1, "Invalid Git command for this SSH connection");
			return;
		}

		long userId = forwarded ? toLong(environments.get(FORWARDED_USER_ID)) : keyOwnerId;
		if (userId <= 0) {
			onExit(-1, "Invalid Git forwarding user");
			return;
		}
		String principal = asUserPrincipal(userId);
		ThreadContext.bind(asSubject(asPrincipals(principal)));
		
		ProjectService projectService = OneDev.getInstance(ProjectService.class);

		var pathArgument = StringUtils.substringAfter(commandString, "'/");
		var projectPath = decodeFullRepoNameAsPath(substringBefore(pathArgument, "'"));
		var projectFacade = projectService.findFacadeByPath(projectPath);
		if (projectFacade == null && projectPath.endsWith(".git")) {
			projectPath = StringUtils.substringBeforeLast(projectPath, ".");
			projectFacade = projectService.findFacadeByPath(projectPath);
		}
		if (StringUtils.isBlank(projectPath))
			throw new ExplicitException("Project not specified");
		if (projectFacade == null) {
			reportProjectNotFoundOrInaccessible(projectPath);
			return;
		}

		Long projectId = projectFacade.getId();
		SessionService sessionService = OneDev.getInstance(SessionService.class);
		sessionService.openSession();
		try {
			Project project = projectService.load(projectId);
			if (!SecurityUtils.canAccessProject(project)) {
				reportProjectNotFoundOrInaccessible(projectPath);
				return;
			}
			if (upload) {
				if (!SecurityUtils.canReadCode(project)) {
					onExit(-1, "You do not have permission to pull from the project");
					return;
				}
			} else if (!SecurityUtils.canWriteCode(project)) {
				onExit(-1, "You do not have permission to push to the project");
				return;
			}
		} finally {
			sessionService.closeSession();
		}

		ClusterService clusterService = OneDev.getInstance(ClusterService.class);
		String activeServerAddress = projectService.getActiveServer(projectId, true);
		if (activeServerAddress.equals(clusterService.getLocalServerAddress())) {
			File gitDir = projectService.getGitDir(projectId);
			Map<String, String> hookEnvs = HookUtils.getReceiveHookEnvs(projectId, principal);

			String groupId = "git-over-ssh-" + projectId + "-" + principal;

			WorkExecutionService workExecutionService = OneDev.getInstance(WorkExecutionService.class);
			future = workExecutionService.submit(PACK_PRIORITY, groupId, new Runnable() {
				
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
		} else if (forwarded) {
			onExit(-1, "The active server for the project has changed. Retry the Git command");
		} else {
			Map<String, String> forwardedEnvironments = new HashMap<>();
			forwardedEnvironments.put(FORWARDED_USER_ID, Long.toString(userId));
			if (protocol != null)
				forwardedEnvironments.put("GIT_PROTOCOL", protocol);
			ExecutorService executorService = OneDev.getInstance(ExecutorService.class);
			future = executorService.submit(() -> {
				SshService sshService = OneDev.getInstance(SshService.class);
				try (var clientSession = sshService.ssh(activeServerAddress);
						var clientChannel = clientSession.createExecChannel(FORWARD_PREFIX + commandString, null, forwardedEnvironments)) {
					clientChannel.setIn(in);
					clientChannel.setOut(out);
					clientChannel.setErr(err);
					clientChannel.open().await(CHANNEL_OPEN_TIMEOUT);
					
					// Do not use clientChannel.waitFor here as it cannot be interrupted
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
		this.in = new FilterInputStream(in) {

			@Override
			public void close() {
			}
			
		};
	}
	
	@Override
	public void setOutputStream(OutputStream out) {
		this.out = new OutputStreamWrapper(out) {
			
			@Override
			public void close() {
			}
			
		};
		((ChannelOutputStream) out).setNoDelay(true);
	}

	@Override
	public void setErrorStream(OutputStream err) {
		this.err = new OutputStreamWrapper(err) {
			
			@Override
			public void close() {
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
