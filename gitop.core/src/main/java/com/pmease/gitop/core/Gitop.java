package com.pmease.gitop.core;

import java.io.Serializable;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Collection;
import java.util.List;

import javax.inject.Inject;

import org.apache.commons.lang3.SerializationUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pmease.commons.loader.AbstractPlugin;
import com.pmease.commons.loader.AppLoader;
import com.pmease.gitop.core.manager.InitManager;
import com.pmease.gitop.core.setting.ServerConfig;

public class Gitop extends AbstractPlugin {

	private static final Logger logger = LoggerFactory.getLogger(Gitop.class);
	
	private final InitManager initManager;
	
	private final ServerConfig serverConfig;
	
	private List<ManualConfig> manualConfigs;
	
	@Inject
	public Gitop(ServerConfig serverConfig, InitManager initManager) {
		this.initManager = initManager;
		this.serverConfig = serverConfig;
	}
	
	@Override
	public Collection<?> getExtensions() {
		return null;
	}

	@SuppressWarnings("serial")
	@Override
	public void start() {
		super.start();
		
		manualConfigs = initManager.init();

		if (!manualConfigs.isEmpty()) synchronized (manualConfigs) {

			final ManualConfig lastConfig = manualConfigs.remove(manualConfigs.size()-1);
			
			manualConfigs.add(new ManualConfig(lastConfig.getSetting()) {

				@Override
				public Skippable getSkippable() {
					return lastConfig.getSkippable();
				}

				@Override
				public void complete() {
					lastConfig.complete();
					synchronized (manualConfigs) {
						manualConfigs.notify();
					}
				}
				
			});
			
			logger.warn("Please point your browser to '" + guessServerUrl() + "' to set up the server.");
			
			try {
				manualConfigs.wait();
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			}
			
			manualConfigs.clear();
		}
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
	
	/**
	 * This method can be called from different UI threads, so we clone manual configs to 
	 * make it thread-safe.
	 * <p>
	 * @return
	 * 			cloned list of manual configs
	 */
	@SuppressWarnings("unchecked")
	public List<ManualConfig> getManualConfigs() {
		synchronized (manualConfigs) {
			return (List<ManualConfig>) SerializationUtils.clone((Serializable) manualConfigs);
		}
	}
	
	public static Gitop getInstance() {
		return AppLoader.getInstance(Gitop.class);
	}
	
	public static <T> T getInstance(Class<T> type) {
		return AppLoader.getInstance(type);
	}

}
