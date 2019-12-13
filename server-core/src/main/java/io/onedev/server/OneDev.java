package io.onedev.server;

import java.io.ObjectStreamException;
import java.io.Serializable;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicReference;

import javax.annotation.Nullable;
import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.onedev.commons.launcher.bootstrap.Bootstrap;
import io.onedev.commons.launcher.loader.AbstractPlugin;
import io.onedev.commons.launcher.loader.AppLoader;
import io.onedev.commons.launcher.loader.ListenerRegistry;
import io.onedev.commons.launcher.loader.ManagedSerializedForm;
import io.onedev.commons.utils.command.Commandline;
import io.onedev.commons.utils.command.LineConsumer;
import io.onedev.server.entitymanager.SettingManager;
import io.onedev.server.event.system.SystemStarted;
import io.onedev.server.event.system.SystemStarting;
import io.onedev.server.event.system.SystemStopped;
import io.onedev.server.event.system.SystemStopping;
import io.onedev.server.maintenance.DataManager;
import io.onedev.server.persistence.PersistManager;
import io.onedev.server.persistence.SessionManager;
import io.onedev.server.persistence.annotation.Sessional;
import io.onedev.server.util.SecurityUtils;
import io.onedev.server.util.ServerConfig;
import io.onedev.server.util.init.InitStage;
import io.onedev.server.util.init.ManualConfig;
import io.onedev.server.util.jetty.JettyRunner;
import io.onedev.server.util.schedule.TaskScheduler;

public class OneDev extends AbstractPlugin implements Serializable {

	public static final String NAME = "OneDev";

	private static final Logger logger = LoggerFactory.getLogger(OneDev.class);
	
	private final JettyRunner jettyRunner;
		
	private final PersistManager persistManager;
	
	private final SessionManager sessionManager;
	
	private final SettingManager configManager;
	
	private final DataManager dataManager;
			
	private final ServerConfig serverConfig;
	
	private final ListenerRegistry listenerRegistry;
	
	private final TaskScheduler taskScheduler;
	
	private final ExecutorService executorService;
	
	private volatile InitStage initStage;
	
	@Inject
	public OneDev(JettyRunner jettyRunner, PersistManager persistManager, TaskScheduler taskScheduler,
			SessionManager sessionManager, ServerConfig serverConfig, DataManager dataManager, 
			SettingManager configManager, ExecutorService executorService, 
			ListenerRegistry listenerRegistry) {
		this.jettyRunner = jettyRunner;
		this.persistManager = persistManager;
		this.taskScheduler = taskScheduler;
		this.sessionManager = sessionManager;
		this.configManager = configManager;
		this.dataManager = dataManager;
		this.serverConfig = serverConfig;
		this.executorService = executorService;
		this.listenerRegistry = listenerRegistry;
		
		initStage = new InitStage("Server is Starting...");
	}
	
	@Override
	public void start() {
		SecurityUtils.bindAsSystem();

		System.setProperty("hsqldb.reconfig_logging", "false");
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
			listenerRegistry.post(new SystemStarting());
		} finally {
			sessionManager.closeSession();
		}
	}
	
	@Sessional
	@Override
	public void postStart() {
		SecurityUtils.bindAsSystem();
		
		listenerRegistry.post(new SystemStarted());
		logger.info("Server is ready at " + configManager.getSystemSetting().getServerUrl() + ".");
		initStage = null;
	}

	public String guessServerUrl() {
		String serverUrl = null;
		
		String serviceHost = System.getenv("ONEDEV_SERVICE_HOST");
		if (serviceHost != null) { // we are running inside Kubernetes  
			Commandline kubectl = new Commandline("kubectl");
			kubectl.addArgs("get", "service", "onedev", "-o", 
					"jsonpath={.status.loadBalancer.ingress[0].ip}");
			AtomicReference<String> externalIpRef = new AtomicReference<>(null);
			kubectl.execute(new LineConsumer() {

				@Override
				public void consume(String line) {
					externalIpRef.set(line);
				}
				
			}, new LineConsumer() {

				@Override
				public void consume(String line) {
					logger.warn(line);
				}
				
			}).checkReturnCode();
			
			if (externalIpRef.get() != null) {
				kubectl.clearArgs();
				kubectl.addArgs("get", "service", "onedev", "-o", 
						"jsonpath={range .spec.ports[*]}{.name} {.port}{'\\n'}{end}");
				AtomicReference<String> httpPortRef = new AtomicReference<>(null);
				AtomicReference<String> httpsPortRef = new AtomicReference<>(null);
				kubectl.execute(new LineConsumer() {

					@Override
					public void consume(String line) {
						String protocol = StringUtils.substringBefore(line, " ");
						if (protocol.equals("http"))
							httpPortRef.set(StringUtils.substringAfter(line, " "));
						else if (protocol.equals("https"))
							httpsPortRef.set(StringUtils.substringAfter(line, " "));
					}
					
				}, new LineConsumer() {

					@Override
					public void consume(String line) {
						logger.warn(line);
					}
					
				}).checkReturnCode();
				
				serverUrl = buildServerUrl(externalIpRef.get(), httpPortRef.get(), httpsPortRef.get());
			} 
			
			if (serverUrl == null) {
				String httpPort = System.getenv("ONEDEV_SERVICE_PORT_HTTP");
				String httpsPort = System.getenv("ONEDEV_SERVICE_PORT_HTTPS");
				serverUrl = buildServerUrl(serviceHost, httpPort, httpsPort);
			}
		} 
		
		if (serverUrl == null) {
			AtomicReference<String> ipRef = new AtomicReference<>(null);
			if (Bootstrap.isInDocker()) {
				Commandline cmd = new Commandline("ip");
				cmd.addArgs("route");
				cmd.execute(new LineConsumer() {

					@Override
					public void consume(String line) {
						if (line.startsWith("default")) {
							StringTokenizer tokenizer = new StringTokenizer(line);
							tokenizer.nextToken();
							tokenizer.nextToken();
							ipRef.set(tokenizer.nextToken());
						}
					}
					
				}, new LineConsumer() {

					@Override
					public void consume(String line) {
						logger.error(line);
					}
					
				}).checkReturnCode();
			}
			
			if (ipRef.get() == null) {
				try {
					if (SystemUtils.IS_OS_MAC_OSX) {
						try (Socket socket = new Socket()) {
							socket.connect(new InetSocketAddress("microsoft.com", 80));
							ipRef.set(StringUtils.stripStart(socket.getLocalAddress().toString(), "/"));					
						} 
					} else {
						try (DatagramSocket socket = new DatagramSocket()) {
							socket.connect(InetAddress.getByName("8.8.8.8"), 10002);
							ipRef.set(socket.getLocalAddress().getHostAddress());
						} 
					}
				} catch (Exception e) {
					try {
						ipRef.set(InetAddress.getLocalHost().getHostName());
					} catch (UnknownHostException e2) {
						throw new RuntimeException(e2);
					}
				}
			}
			
			if (serverConfig.getHttpPort() != 0)
				serverUrl = "http://" + ipRef.get() + ":" + serverConfig.getHttpPort();
			else 
				serverUrl = "https://" + ipRef.get() + ":" + serverConfig.getHttpsPort();
		}
		
		return serverUrl;
	}
	
	private String buildServerUrl(String host, @Nullable String httpPort, @Nullable String httpsPort) {
		String serverUrl = null;
		if (httpPort != null) {
			serverUrl = "http://" + host;
			if (!httpPort.equals("80"))
				serverUrl += ":" + httpPort;
		} else if (httpsPort != null) {
			serverUrl = "https://" + host;
			if (!httpsPort.equals("443"))
				serverUrl += ":" + httpsPort;
		} else {
			logger.warn("This OneDev deployment looks odd to me: "
					+ "both http and https port are not specified");
		}
		return serverUrl;
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
		SecurityUtils.bindAsSystem();
		listenerRegistry.post(new SystemStopping());
	}

	@Override
	public void stop() {
		SecurityUtils.bindAsSystem();
		
		sessionManager.openSession();
		try {
			listenerRegistry.post(new SystemStopped());
		} finally {
			sessionManager.closeSession();
		}
		persistManager.stop();
		
		taskScheduler.stop();
		jettyRunner.stop();
		executorService.shutdown();
	}
		
	public Object writeReplace() throws ObjectStreamException {
		return new ManagedSerializedForm(OneDev.class);
	}	
	
	public String getDocRoot() {
		return "https://github.com/theonedev/onedev/wiki";
	}
	
}
