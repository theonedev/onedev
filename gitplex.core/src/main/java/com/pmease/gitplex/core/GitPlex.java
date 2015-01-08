package com.pmease.gitplex.core;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Set;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Provider;

import org.apache.commons.lang3.StringUtils;
import org.quartz.ScheduleBuilder;
import org.quartz.SimpleScheduleBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pmease.commons.git.GitConfig;
import com.pmease.commons.git.command.GitCommand;
import com.pmease.commons.loader.AbstractPlugin;
import com.pmease.commons.loader.AppLoader;
import com.pmease.commons.loader.AppName;
import com.pmease.commons.schedule.SchedulableTask;
import com.pmease.commons.schedule.TaskScheduler;
import com.pmease.commons.util.init.InitStage;
import com.pmease.commons.util.init.ManualConfig;
import com.pmease.gitplex.core.manager.DataManager;
import com.pmease.gitplex.core.manager.PullRequestManager;
import com.pmease.gitplex.core.manager.RepositoryManager;
import com.pmease.gitplex.core.manager.UserManager;
import com.pmease.gitplex.core.setting.ServerConfig;

public class GitPlex extends AbstractPlugin {

	private static final Logger logger = LoggerFactory.getLogger(GitPlex.class);
	
	private final DataManager dataManager;
	
	private final RepositoryManager repositoryManager;
	
	private final UserManager userManager;
	
	private final PullRequestManager pullRequestManager;
	
	private final ServerConfig serverConfig;

	private final String appName;
	
	private volatile InitStage initStage;
	
	private final TaskScheduler taskScheduler;
	
	private final Provider<GitConfig> gitConfigProvider;
	
	private volatile String gitError;
	
	private String gitCheckTaskId;
	
	@Inject
	public GitPlex(ServerConfig serverConfig, DataManager dataManager, 
			PullRequestManager pullRequestManager,
            RepositoryManager repositoryManager, UserManager userManager,
            TaskScheduler taskScheduler, Provider<GitConfig> gitConfigProvider,
            @AppName String appName) {
		this.dataManager = dataManager;
		this.repositoryManager = repositoryManager;
		this.userManager = userManager;
		this.pullRequestManager = pullRequestManager;
		this.serverConfig = serverConfig;
		this.taskScheduler = taskScheduler;
		this.gitConfigProvider = gitConfigProvider;
		
		this.appName = appName;
		
		initStage = new InitStage("Server is Starting...");
	}
	
	@Override
	public void start() {
		List<ManualConfig> manualConfigs = dataManager.init();
		
		if (!manualConfigs.isEmpty()) {
			logger.warn("Please set up the server at " + GitPlex.getInstance().getServerUrl() + ".");
			initStage = new InitStage("Server Setup", manualConfigs);
			
			initStage.waitFor();
		}

		userManager.start();
		repositoryManager.start();
		pullRequestManager.start();
		
		gitCheckTaskId = taskScheduler.schedule(new SchedulableTask() {
			
			@Override
			public ScheduleBuilder<?> getScheduleBuilder() {
				return SimpleScheduleBuilder.repeatHourlyForever();
			}
			
			@Override
			public void execute() {
				checkGit();
			}
			
		});
		checkGit();
		
		logger.info("Checking repositories...");
		
		repositoryManager.checkSanity();
	}
	
	public void checkGit() {
		gitError = GitCommand.checkError(gitConfigProvider.get().getExecutable());
	}
	
	@Override
	public void postStart() {
		initStage = null;
		
		logger.info("Server is ready at " + getServerUrl() + ".");
	}

	public String getServerUrl() {
		String hostName;
		try {
			hostName = InetAddress.getLocalHost().getHostName();
		} catch (UnknownHostException e) {
			throw new RuntimeException(e);
		}
		
		String serverUrl;
		if (serverConfig.getHttpPort() != 0)
			serverUrl = "http://" + hostName + ":" + serverConfig.getHttpPort();
		else 
			serverUrl = "https://" + hostName + ":" + serverConfig.getSslConfig().getPort();

		return StringUtils.stripEnd(serverUrl + serverConfig.getContextPath(), "/");
	}
	
	/**
	 * Get context aware servlet request path given path inside context. For instance if 
	 * pathInContext is &quot;/images/ok.png&quot;, this method will return 
	 * &quot;/gitplex/images/ok.png&quot; if GitPlex web UI is configured to run under
	 * context path &quot;/gitplex&quot;
	 *  
	 * @param pathInContext
	 * 			servlet request path inside servlet context. It does not matter whether or 
	 * 			not this path starts with slash 
	 * @return
	 * 			absolute path prepending servlet context path
	 */
	public String getContextAwarePath(String pathInContext) {
		String contextAwarePath = serverConfig.getContextPath();
		contextAwarePath = StringUtils.stripEnd(contextAwarePath, "/");
		if (pathInContext.startsWith("/"))
			return contextAwarePath + pathInContext;
		else
			return contextAwarePath + "/" + pathInContext;
	}
	
	public String getAppName() {
		return appName;
	}
	
	/**
	 * This method can be called from different UI threads, so we clone initStage to 
	 * make it thread-safe.
	 * <p>
	 * @return
	 * 			cloned initStage, or <tt>null</tt> if system initialization is completed
	 */
	public @Nullable InitStage getInitStage() {
		if (initStage != null) {
			return initStage.clone();
		} else {
			return null;
		}
	}
	
	public boolean isReady() {
		return initStage == null;
	}
	
	public static GitPlex getInstance() {
		return AppLoader.getInstance(GitPlex.class);
	}
	
	public static <T> T getInstance(Class<T> type) {
		return AppLoader.getInstance(type);
	}

	public static <T> Set<T> getExtensions(Class<T> extensionPoint) {
		return AppLoader.getExtensions(extensionPoint);
	}

	@Override
	public void stop() {
		taskScheduler.unschedule(gitCheckTaskId);
		pullRequestManager.stop();
		userManager.stop();
		repositoryManager.stop();
	}
	
	public String getGitError() {
		return gitError;
	}

}
