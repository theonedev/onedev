package com.gitplex.server.core;

import java.io.ObjectStreamException;
import java.io.Serializable;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Set;

import javax.annotation.Nullable;
import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.util.ThreadContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gitplex.commons.hibernate.Sessional;
import com.gitplex.commons.loader.AbstractPlugin;
import com.gitplex.commons.loader.AppLoader;
import com.gitplex.commons.loader.ListenerRegistry;
import com.gitplex.commons.loader.ManagedSerializedForm;
import com.gitplex.commons.util.init.InitStage;
import com.gitplex.commons.util.init.ManualConfig;
import com.gitplex.server.core.event.lifecycle.SystemStarted;
import com.gitplex.server.core.event.lifecycle.SystemStarting;
import com.gitplex.server.core.event.lifecycle.SystemStopped;
import com.gitplex.server.core.event.lifecycle.SystemStopping;
import com.gitplex.server.core.manager.AccountManager;
import com.gitplex.server.core.manager.ConfigManager;
import com.gitplex.server.core.manager.DataManager;
import com.gitplex.server.core.setting.ServerConfig;

public class GitPlex extends AbstractPlugin implements Serializable {

	private static final Logger logger = LoggerFactory.getLogger(GitPlex.class);
	
	private final ConfigManager configManager;
	
	private final DataManager dataManager;
	
	private final AccountManager accountManager;
	
	private final ServerConfig serverConfig;
	
	private final ListenerRegistry listenerRegistry;
	
	private volatile InitStage initStage;
	
	@Inject
	public GitPlex(ServerConfig serverConfig, DataManager dataManager, ConfigManager configManager,
            AccountManager accountManager, ListenerRegistry listenerRegistry) {
		this.configManager = configManager;
		this.dataManager = dataManager;
		this.serverConfig = serverConfig;
		this.accountManager = accountManager;
		this.listenerRegistry = listenerRegistry;
		
		initStage = new InitStage("Server is Starting...");
	}
	
	@Override
	@Sessional
	public void start() {
		List<ManualConfig> manualConfigs = dataManager.init();
		
		if (!manualConfigs.isEmpty()) {
			logger.warn("Please set up the server at " + guessServerUrl());
			initStage = new InitStage("Server Setup", manualConfigs);
			
			initStage.waitFor();
		}

		ThreadContext.bind(accountManager.getRoot().asSubject());
		
		listenerRegistry.post(new SystemStarting());
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
		ThreadContext.bind(accountManager.getRoot().asSubject());
		listenerRegistry.post(new SystemStopping());
	}

	@Sessional
	@Override
	public void stop() {
		listenerRegistry.post(new SystemStopped());
		ThreadContext.unbindSubject();
	}
	
	public Object writeReplace() throws ObjectStreamException {
		return new ManagedSerializedForm(GitPlex.class);
	}	
	
	public String getDocLink() {
		return "http://wiki.pmease.com//display/gp" + StringUtils.substringBefore(AppLoader.getProduct().getVersion(), ".");
	}
	
}
