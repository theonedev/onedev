package com.pmease.gitplex.core;

import java.io.File;
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

import com.google.common.eventbus.EventBus;
import com.pmease.commons.git.Git;
import com.pmease.commons.git.GitConfig;
import com.pmease.commons.git.command.GitCommand;
import com.pmease.commons.loader.AbstractPlugin;
import com.pmease.commons.loader.AppLoader;
import com.pmease.commons.loader.AppName;
import com.pmease.commons.schedule.SchedulableTask;
import com.pmease.commons.schedule.TaskScheduler;
import com.pmease.commons.util.init.InitStage;
import com.pmease.commons.util.init.ManualConfig;
import com.pmease.gitplex.core.events.SystemStarted;
import com.pmease.gitplex.core.events.SystemStarting;
import com.pmease.gitplex.core.events.SystemStopped;
import com.pmease.gitplex.core.events.SystemStopping;
import com.pmease.gitplex.core.manager.DataManager;
import com.pmease.gitplex.core.setting.ServerConfig;

public class GitPlex extends AbstractPlugin {

	private static final Logger logger = LoggerFactory.getLogger(GitPlex.class);
	
	private final EventBus eventBus;
	
	private final DataManager dataManager;
	
	private final ServerConfig serverConfig;

	private final String appName;
	
	private volatile InitStage initStage;
	
	private final TaskScheduler taskScheduler;
	
	private final Provider<GitConfig> gitConfigProvider;
	
	private volatile String gitError;
	
	private String gitCheckTaskId;
	
	@Inject
	public GitPlex(EventBus eventBus, ServerConfig serverConfig, DataManager dataManager, 
            TaskScheduler taskScheduler, Provider<GitConfig> gitConfigProvider,
            @AppName String appName) {
		this.eventBus = eventBus;
		this.dataManager = dataManager;
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
			logger.warn("Please set up the server at " + GitPlex.getInstance().guessServerUrl() + ".");
			initStage = new InitStage("Server Setup", manualConfigs);
			
			initStage.waitFor();
		}

		eventBus.post(new SystemStarting());
		
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

		/*
		String gitDir = "w:\\linux";
		String folder = "kernel";
		Git git = new Git(new File(gitDir + "/.git"));
		long time = System.currentTimeMillis();
		int size = 0;
		for (File file: new File(gitDir + "/" + folder).listFiles()) {
			if (folder.length() != 0)
				size += git.log(null, "master", folder + "/" + file.getName(), 1, 0).size();
			else
				size += git.log(null, "master", file.getName(), 1, 0).size();
//			System.out.println(size);
		}
		System.out.println(System.currentTimeMillis()-time);
		*/
	}
	
	public void checkGit() {
		gitError = GitCommand.checkError(gitConfigProvider.get().getExecutable());
	}
	
	@Override
	public void postStart() {
		initStage = null;
		
		eventBus.post(new SystemStarted());
		
		logger.info("Server is ready at " + guessServerUrl() + ".");
	}

	public String guessServerUrl() {
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

		return StringUtils.stripEnd(serverUrl, "/");
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
	public void preStop() {
		eventBus.post(new SystemStopping());
	}

	@Override
	public void stop() {
		taskScheduler.unschedule(gitCheckTaskId);
		eventBus.post(new SystemStopped());
	}
	
	public String getGitError() {
		return gitError;
	}

}
