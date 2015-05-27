package com.pmease.gitplex.core;

import java.io.ObjectStreamException;
import java.io.Serializable;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Set;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Provider;
import javax.validation.ConstraintValidatorContext;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.util.ThreadContext;
import org.hibernate.validator.constraints.NotEmpty;
import org.quartz.ScheduleBuilder;
import org.quartz.SimpleScheduleBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.eventbus.EventBus;
import com.pmease.commons.editable.annotation.Editable;
import com.pmease.commons.git.GitConfig;
import com.pmease.commons.git.command.GitCommand;
import com.pmease.commons.hibernate.Sessional;
import com.pmease.commons.loader.AbstractPlugin;
import com.pmease.commons.loader.AppLoader;
import com.pmease.commons.loader.AppName;
import com.pmease.commons.loader.ManagedSerializedForm;
import com.pmease.commons.schedule.SchedulableTask;
import com.pmease.commons.schedule.TaskScheduler;
import com.pmease.commons.util.init.InitStage;
import com.pmease.commons.util.init.ManualConfig;
import com.pmease.commons.util.init.Skippable;
import com.pmease.commons.validation.ClassValidating;
import com.pmease.commons.validation.Validatable;
import com.pmease.gitplex.core.events.SystemStarted;
import com.pmease.gitplex.core.events.SystemStarting;
import com.pmease.gitplex.core.events.SystemStopped;
import com.pmease.gitplex.core.events.SystemStopping;
import com.pmease.gitplex.core.manager.DataManager;
import com.pmease.gitplex.core.manager.UserManager;
import com.pmease.gitplex.core.setting.ServerConfig;

public class GitPlex extends AbstractPlugin implements Serializable {

	private static final Logger logger = LoggerFactory.getLogger(GitPlex.class);
	
	private final EventBus eventBus;
	
	private final DataManager dataManager;
	
	private final UserManager userManager;
	
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
            UserManager userManager, @AppName String appName) {
		this.eventBus = eventBus;
		this.dataManager = dataManager;
		this.serverConfig = serverConfig;
		this.taskScheduler = taskScheduler;
		this.gitConfigProvider = gitConfigProvider;
		this.userManager = userManager;
		
		this.appName = appName;
		
		initStage = new InitStage("Server is Starting...");
	}
	
	@SuppressWarnings("serial")
	@Override
	@Sessional
	public void start() {
		List<ManualConfig> manualConfigs = dataManager.init();
		
		if (!manualConfigs.isEmpty()) {
			CredentialBean.presetCredential = RandomStringUtils.randomAlphanumeric(10);
			CredentialBean bean = new CredentialBean();
			manualConfigs.add(0, new ManualConfig("Supply credential printed on console", bean) {

				@Override
				public Skippable getSkippable() {
					return null;
				}

				@Override
				public void complete() {
					
				}
				
			});
			
			logger.warn("Please set up the server at " + GitPlex.getInstance().guessServerUrl() 
					+ " with below credential:\n" + CredentialBean.presetCredential);
			initStage = new InitStage("Server Setup", manualConfigs);
			
			initStage.waitFor();
		}

		ThreadContext.bind(userManager.getRoot().asSubject());
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
	}
	
	public void checkGit() {
		gitError = GitCommand.checkError(gitConfigProvider.get().getExecutable());
	}
	
	@Sessional
	@Override
	public void postStart() {
		initStage = null;
		
		eventBus.post(new SystemStarted());
		
		ThreadContext.unbindSubject();
		
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

	@Sessional
	@Override
	public void preStop() {
		ThreadContext.bind(userManager.getRoot().asSubject());
		eventBus.post(new SystemStopping());
	}

	@Sessional
	@Override
	public void stop() {
		taskScheduler.unschedule(gitCheckTaskId);
		eventBus.post(new SystemStopped());
		ThreadContext.unbindSubject();
	}
	
	public String getGitError() {
		return gitError;
	}

	@SuppressWarnings("serial")
	@Editable
	@ClassValidating
	public static class CredentialBean implements Serializable, Validatable {
		
		static String presetCredential; 
		
		private String inputCredential;

		@Editable(name="Console Credential", description="For security reason, GitPlex prints out a credential at "
				+ "console and you need to copy it here to continue with server setup.")
		@NotEmpty
		public String getInputCredential() {
			return inputCredential;
		}

		public void setInputCredential(String inputCredential) {
			this.inputCredential = inputCredential;
		}

		@Override
		public boolean isValid(ConstraintValidatorContext context) {
			if (inputCredential != null && !inputCredential.equals(presetCredential)) {
				context.disableDefaultConstraintViolation();
				String message = "Supplied credential is incorrect, please check GitPlex console";
				context.buildConstraintViolationWithTemplate(message).addBeanNode().addConstraintViolation();
				return false;
			} else {
				return true;
			}
		}
		
	}
	
	public Object writeReplace() throws ObjectStreamException {
		return new ManagedSerializedForm(GitPlex.class);
	}	
	
}
