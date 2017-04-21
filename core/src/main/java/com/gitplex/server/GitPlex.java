package com.gitplex.server;

import java.io.ObjectStreamException;
import java.io.Serializable;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Nullable;
import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.util.ThreadContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gitplex.launcher.bootstrap.Bootstrap;
import com.gitplex.launcher.loader.AbstractPlugin;
import com.gitplex.launcher.loader.AppLoader;
import com.gitplex.launcher.loader.ListenerRegistry;
import com.gitplex.launcher.loader.ManagedSerializedForm;
import com.gitplex.server.event.lifecycle.SystemStarted;
import com.gitplex.server.event.lifecycle.SystemStarting;
import com.gitplex.server.event.lifecycle.SystemStopped;
import com.gitplex.server.event.lifecycle.SystemStopping;
import com.gitplex.server.manager.AccountManager;
import com.gitplex.server.manager.ConfigManager;
import com.gitplex.server.manager.DataManager;
import com.gitplex.server.persistence.PersistManager;
import com.gitplex.server.persistence.UnitOfWork;
import com.gitplex.server.persistence.annotation.Sessional;
import com.gitplex.server.persistence.dao.Dao;
import com.gitplex.server.util.init.InitStage;
import com.gitplex.server.util.init.ManualConfig;
import com.gitplex.server.util.jetty.JettyRunner;
import com.gitplex.server.util.schedule.TaskScheduler;
import com.gitplex.server.util.serverconfig.ServerConfig;

public class GitPlex extends AbstractPlugin implements Serializable {

	public static final String NAME = "GitPlex";
	
	private static final Logger logger = LoggerFactory.getLogger(GitPlex.class);
	
	private static final Pattern DOCLINK_PATTERN = Pattern.compile("\\d+\\.\\d+");
	
	private final JettyRunner jettyRunner;
	
	private final TaskScheduler taskScheduler;
	
	private final PersistManager persistManager;
	
	private final Dao dao;
	
	private final UnitOfWork unitOfWork;
	
	private final ConfigManager configManager;
	
	private final DataManager dataManager;
	
	private final AccountManager accountManager;
	
	private final ServerConfig serverConfig;
	
	private final ListenerRegistry listenerRegistry;
	
	private volatile InitStage initStage;
	
	@Inject
	public GitPlex(JettyRunner jettyRunner, TaskScheduler taskScheduler, PersistManager persistManager, 
			Dao dao, UnitOfWork unitOfWork, ServerConfig serverConfig, DataManager dataManager, 
			ConfigManager configManager, AccountManager accountManager, ListenerRegistry listenerRegistry) {
		this.jettyRunner = jettyRunner;
		this.taskScheduler = taskScheduler;
		this.persistManager = persistManager;
		this.dao = dao;
		this.unitOfWork = unitOfWork;
		this.configManager = configManager;
		this.dataManager = dataManager;
		this.serverConfig = serverConfig;
		this.accountManager = accountManager;
		this.listenerRegistry = listenerRegistry;
		
		initStage = new InitStage("Server is Starting...");
	}
	
	@Override
	public void start() {
		jettyRunner.start();
		
		if (Bootstrap.command == null) {
			taskScheduler.start();
		}
		
		persistManager.start();
		
		unitOfWork.begin();
		try {
			List<ManualConfig> manualConfigs = dataManager.init();
			
			if (!manualConfigs.isEmpty()) {
				logger.warn("Please set up the server at " + guessServerUrl());
				initStage = new InitStage("Server Setup", manualConfigs);
				
				initStage.waitForFinish();
				
				// clear session in order to pick up changes made in interactive setup 
				dao.getSession().clear();
			}

			ThreadContext.bind(accountManager.getRoot().asSubject());
			
			listenerRegistry.post(new SystemStarting());
		} finally {
			unitOfWork.end();
		}
	}
	
	@Sessional
	@Override
	public void postStart() {
		initStage = null;

		listenerRegistry.post(new SystemStarted());
		
		ThreadContext.unbindSubject();
		
		logger.info("Server is ready at " + configManager.getSystemSetting().getServerUrl() + ".");
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
	
	/**
	 * This method can be called from different UI threads, so we clone initStage to 
	 * make it thread-safe.
	 * <p>
	 * @return
	 * 			cloned initStage, or <tt>null</tt> if system initialization is completed
	 */
	public @Nullable InitStage getInitStage() {
		return initStage;
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
		ThreadContext.bind(accountManager.getRoot().asSubject());
		listenerRegistry.post(new SystemStopping());
	}

	@Override
	public void stop() {
		unitOfWork.begin();
		try {
			listenerRegistry.post(new SystemStopped());
			ThreadContext.unbindSubject();
		} finally {
			unitOfWork.end();
		}
		persistManager.stop();
		
		taskScheduler.stop();
		jettyRunner.stop();
	}
		
	public Object writeReplace() throws ObjectStreamException {
		return new ManagedSerializedForm(GitPlex.class);
	}	
	
	public String getDocLink() {
		String productVersion = AppLoader.getProduct().getVersion();
		Matcher matcher = DOCLINK_PATTERN.matcher(productVersion);
		if (!matcher.find())
			throw new RuntimeException("Unexpected product version format: " + productVersion);
		String wikiSpace = "GP" + matcher.group().replace(".", "");
		return "http://wiki.pmease.com//display/" + wikiSpace;
	}
	
}
