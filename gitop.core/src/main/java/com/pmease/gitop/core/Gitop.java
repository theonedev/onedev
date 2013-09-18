package com.pmease.gitop.core;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Set;

import javax.annotation.Nullable;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pmease.commons.loader.AbstractPlugin;
import com.pmease.commons.loader.AppLoader;
import com.pmease.commons.loader.AppName;
import com.pmease.commons.util.init.InitStage;
import com.pmease.commons.util.init.ManualConfig;
import com.pmease.gitop.core.manager.DataManager;
import com.pmease.gitop.core.setting.ServerConfig;

public class Gitop extends AbstractPlugin {

	private static final Logger logger = LoggerFactory.getLogger(Gitop.class);
	
	private final DataManager dataManager;
	
	private final ServerConfig serverConfig;

	private final String appName;
	
	private volatile InitStage initStage;
	
	@Inject
	public Gitop(ServerConfig serverConfig, DataManager dataManager, @AppName String appName) {
		this.dataManager = dataManager;
		this.serverConfig = serverConfig;
		this.appName = appName;
		
		initStage = new InitStage("Server is Starting...");
	}
	
	@Override
	public void start() {
		List<ManualConfig> manualConfigs = dataManager.init();
		
		if (!manualConfigs.isEmpty()) {
			logger.warn("Please set up the server at " + Gitop.getInstance().guessServerUrl() + ".");
			initStage = new InitStage("Server Setup", manualConfigs);
			
			initStage.waitFor();
		}
	}
	
	@Override
	public void postStart() {
		initStage = null;
		
		logger.info("Server is ready at " + guessServerUrl() + ".");
	}

	public String guessServerUrl() {
		String hostName;
		try {
			hostName = InetAddress.getLocalHost().getHostName();
		} catch (UnknownHostException e) {
			throw new RuntimeException(e);
		}
		
		if (serverConfig.getHttpPort() != 0)
			return "http://" + hostName + ":" + serverConfig.getHttpPort();
		else 
			return "https://" + hostName + ":" + serverConfig.getSslConfig().getPort();
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
	
	public static Gitop getInstance() {
		return AppLoader.getInstance(Gitop.class);
	}
	
	public static <T> T getInstance(Class<T> type) {
		return AppLoader.getInstance(type);
	}

	public static <T> Set<T> getExtensions(Class<T> extensionPoint) {
		return AppLoader.getExtensions(extensionPoint);
	}

	@Override
	public void stop() {
	}
	
}
