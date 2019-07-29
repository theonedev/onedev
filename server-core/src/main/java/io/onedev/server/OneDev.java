package io.onedev.server;

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

import io.onedev.commons.launcher.bootstrap.Bootstrap;
import io.onedev.commons.launcher.loader.AbstractPlugin;
import io.onedev.commons.launcher.loader.AppLoader;
import io.onedev.commons.launcher.loader.ListenerRegistry;
import io.onedev.commons.launcher.loader.ManagedSerializedForm;
import io.onedev.commons.utils.init.InitStage;
import io.onedev.commons.utils.init.ManualConfig;
import io.onedev.commons.utils.schedule.TaskScheduler;
import io.onedev.server.entitymanager.SettingManager;
import io.onedev.server.entitymanager.UserManager;
import io.onedev.server.event.system.SystemStarted;
import io.onedev.server.event.system.SystemStarting;
import io.onedev.server.event.system.SystemStopped;
import io.onedev.server.event.system.SystemStopping;
import io.onedev.server.maintenance.DataManager;
import io.onedev.server.persistence.PersistManager;
import io.onedev.server.persistence.SessionManager;
import io.onedev.server.persistence.annotation.Sessional;
import io.onedev.server.util.ServerConfig;
import io.onedev.server.util.jetty.JettyRunner;

public class OneDev extends AbstractPlugin implements Serializable {

	public static final String NAME = "OneDev";
	
	private static final Logger logger = LoggerFactory.getLogger(OneDev.class);
	
	private final JettyRunner jettyRunner;
		
	private final PersistManager persistManager;
	
	private final SessionManager sessionManager;
	
	private final SettingManager configManager;
	
	private final DataManager dataManager;
			
	private final UserManager userManager;
	
	private final ServerConfig serverConfig;
	
	private final ListenerRegistry listenerRegistry;
	
	private final TaskScheduler taskScheduler;
	
	private volatile InitStage initStage;
	
	@Inject
	public OneDev(JettyRunner jettyRunner, PersistManager persistManager, TaskScheduler taskScheduler,
			SessionManager sessionManager, ServerConfig serverConfig, DataManager dataManager, 
			SettingManager configManager, UserManager userManager, ListenerRegistry listenerRegistry) {
		this.jettyRunner = jettyRunner;
		this.persistManager = persistManager;
		this.taskScheduler = taskScheduler;
		this.sessionManager = sessionManager;
		this.configManager = configManager;
		this.dataManager = dataManager;
		this.serverConfig = serverConfig;
		this.userManager = userManager;
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
		
		List<ManualConfig> manualConfigs = dataManager.init();
		if (!manualConfigs.isEmpty()) {
			logger.warn("Please set up the server at " + guessServerUrl());
			initStage = new InitStage("Server Setup", manualConfigs);
			
			initStage.waitForFinish();
		}

		sessionManager.openSession();
		try {
			ThreadContext.bind(userManager.getRoot().asSubject());
			listenerRegistry.post(new SystemStarting());
		} finally {
			sessionManager.closeSession();
		}
	}
	
	@Sessional
	@Override
	public void postStart() {
		listenerRegistry.post(new SystemStarted());
		ThreadContext.unbindSubject();
		logger.info("Server is ready at " + configManager.getSystemSetting().getServerUrl() + ".");
		initStage = null;
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
			serverUrl = "https://" + hostName + ":" + serverConfig.getHttpsPort();

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
	
	public static OneDev getInstance() {
		return AppLoader.getInstance(OneDev.class);
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
		listenerRegistry.post(new SystemStopping());
	}

	@Override
	public void stop() {
		sessionManager.openSession();
		try {
			listenerRegistry.post(new SystemStopped());
			ThreadContext.unbindSubject();
		} finally {
			sessionManager.closeSession();
		}
		persistManager.stop();
		
		taskScheduler.stop();
		jettyRunner.stop();
	}
		
	public Object writeReplace() throws ObjectStreamException {
		return new ManagedSerializedForm(OneDev.class);
	}	
	
	public String getDocRoot() {
		return "https://github.com/theonedev/onedev/wiki";
	}
	
}
