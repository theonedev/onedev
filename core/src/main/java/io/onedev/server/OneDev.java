package io.onedev.server;

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

import io.onedev.launcher.bootstrap.Bootstrap;
import io.onedev.launcher.loader.AbstractPlugin;
import io.onedev.launcher.loader.AppLoader;
import io.onedev.launcher.loader.ListenerRegistry;
import io.onedev.launcher.loader.ManagedSerializedForm;
import io.onedev.server.event.lifecycle.SystemStarted;
import io.onedev.server.event.lifecycle.SystemStarting;
import io.onedev.server.event.lifecycle.SystemStopped;
import io.onedev.server.event.lifecycle.SystemStopping;
import io.onedev.server.manager.ConfigManager;
import io.onedev.server.manager.DataManager;
import io.onedev.server.manager.UserManager;
import io.onedev.server.persistence.PersistManager;
import io.onedev.server.persistence.UnitOfWork;
import io.onedev.server.persistence.annotation.Sessional;
import io.onedev.server.util.jetty.JettyRunner;
import io.onedev.server.util.serverconfig.ServerConfig;
import io.onedev.server.web.websocket.WebSocketManager;
import io.onedev.utils.init.InitStage;
import io.onedev.utils.init.ManualConfig;
import io.onedev.utils.schedule.TaskScheduler;

public class OneDev extends AbstractPlugin implements Serializable {

	public static final String NAME = "OneDev";
	
	private static final Logger logger = LoggerFactory.getLogger(OneDev.class);
	
	private static final Pattern DOCLINK_PATTERN = Pattern.compile("\\d+\\.\\d+");
	
	private final JettyRunner jettyRunner;
	
	private final PersistManager persistManager;
	
	private final UnitOfWork unitOfWork;
	
	private final ConfigManager configManager;
	
	private final DataManager dataManager;
	
	private final UserManager userManager;
	
	private final ServerConfig serverConfig;
	
	private final ListenerRegistry listenerRegistry;
	
	private final TaskScheduler taskScheduler;
	
	private volatile InitStage initStage;
	
	private final WebSocketManager webSocketManager;
	
	@Inject
	public OneDev(JettyRunner jettyRunner, PersistManager persistManager, TaskScheduler taskScheduler,
			UnitOfWork unitOfWork, ServerConfig serverConfig, DataManager dataManager, ConfigManager configManager, 
			UserManager userManager, ListenerRegistry listenerRegistry, WebSocketManager webSocketManager) {
		this.jettyRunner = jettyRunner;
		this.persistManager = persistManager;
		this.taskScheduler = taskScheduler;
		this.unitOfWork = unitOfWork;
		this.configManager = configManager;
		this.dataManager = dataManager;
		this.serverConfig = serverConfig;
		this.userManager = userManager;
		this.listenerRegistry = listenerRegistry;
		this.webSocketManager = webSocketManager;
		
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

		unitOfWork.begin();
		try {
			ThreadContext.bind(userManager.getRoot().asSubject());
			listenerRegistry.post(new SystemStarting());
		} finally {
			unitOfWork.end();
		}
		
		webSocketManager.start();
	}
	
	@Sessional
	@Override
	public void postStart() {
		webSocketManager.stop();
		
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
		return new ManagedSerializedForm(OneDev.class);
	}	
	
	public String getDocLink() {
		String productVersion = AppLoader.getProduct().getVersion();
		Matcher matcher = DOCLINK_PATTERN.matcher(productVersion);
		if (!matcher.find())
			throw new RuntimeException("Unexpected product version format: " + productVersion);
		return "https://go.onedev.io/projects/onedev-docs/blob/" + matcher.group();
	}
	
}
