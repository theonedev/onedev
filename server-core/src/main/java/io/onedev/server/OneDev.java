package io.onedev.server;

import java.io.ObjectStreamException;
import java.io.Serializable;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicReference;

import javax.annotation.Nullable;
import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;
import org.apache.wicket.request.Url;
import org.apache.wicket.request.Url.StringMode;
import org.eclipse.jgit.util.FS.FileStoreAttributes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Provider;

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
import io.onedev.server.model.support.administration.SystemSetting;
import io.onedev.server.persistence.PersistManager;
import io.onedev.server.persistence.SessionManager;
import io.onedev.server.persistence.annotation.Sessional;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.util.ServerConfig;
import io.onedev.server.util.Version;
import io.onedev.server.util.init.InitStage;
import io.onedev.server.util.init.ManualConfig;
import io.onedev.server.util.jetty.JettyLauncher;
import io.onedev.server.util.schedule.TaskScheduler;

public class OneDev extends AbstractPlugin implements Serializable {

	public static final String NAME = "OneDev";

	private static final Logger logger = LoggerFactory.getLogger(OneDev.class);
	
	private final Provider<JettyLauncher> jettyLauncherProvider;
		
	private final PersistManager persistManager;
	
	private final SessionManager sessionManager;
	
	private final SettingManager settingManager;
	
	private final DataManager dataManager;
			
	private final Provider<ServerConfig> serverConfigProvider;
	
	private final ListenerRegistry listenerRegistry;
	
	private final TaskScheduler taskScheduler;
	
	private final ExecutorService executorService;
	
	private volatile InitStage initStage;

	// Some are injected via provider as instantiation might encounter problem during upgrade 
	@Inject
	public OneDev(Provider<JettyLauncher> jettyLauncherProvider, PersistManager persistManager, 
			TaskScheduler taskScheduler, SessionManager sessionManager, 
			Provider<ServerConfig> serverConfigProvider, DataManager dataManager, 
			SettingManager settingManager, ExecutorService executorService, 
			ListenerRegistry listenerRegistry) {
		this.jettyLauncherProvider = jettyLauncherProvider;
		this.persistManager = persistManager;
		this.taskScheduler = taskScheduler;
		this.sessionManager = sessionManager;
		this.settingManager = settingManager;
		this.dataManager = dataManager;
		this.serverConfigProvider = serverConfigProvider;
		this.executorService = executorService;
		this.listenerRegistry = listenerRegistry;
		
		initStage = new InitStage("Server is Starting...");
	}
	
	@Override
	public void start() {
		SecurityUtils.bindAsSystem();

		System.setProperty("hsqldb.reconfig_logging", "false");
		
		if (Bootstrap.command == null) {
			jettyLauncherProvider.get().start();
			taskScheduler.start();
		}
		
		persistManager.start();
		
		List<ManualConfig> manualConfigs = dataManager.init();
		if (!manualConfigs.isEmpty()) {
			String serverUrl = StringUtils.stripEnd(guessServerUrl(false).toString(StringMode.FULL), "/");
			logger.warn("Please set up the server at " + serverUrl);
			initStage = new InitStage("Server Setup", manualConfigs);
			
			initStage.waitForFinish();
		}
		
		sessionManager.openSession();
		try {
			listenerRegistry.post(new SystemStarting());
		} finally {
			sessionManager.closeSession();
		}
		
		// workaround for issue https://bugs.eclipse.org/bugs/show_bug.cgi?id=566170
		FileStoreAttributes.setBackground(true);
	}
	
	@Sessional
	@Override
	public void postStart() {
		SecurityUtils.bindAsSystem();
		
		listenerRegistry.post(new SystemStarted());
		SystemSetting systemSetting = settingManager.getSystemSetting();
        logger.info("Server is ready at " + systemSetting.getServerUrl() + ".");
		initStage = null;
	}

	public Url guessServerUrl(boolean ssh) {
	    Url serverUrl = null;
		
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
			
			String externalIp = externalIpRef.get();
			
			if (externalIp != null) {
				kubectl.clearArgs();
				kubectl.addArgs("get", "service", "onedev", "-o", 
						"jsonpath={range .spec.ports[*]}{.name} {.port}{'\\n'}{end}");
				AtomicReference<Integer> httpPortRef = new AtomicReference<>(null);
				AtomicReference<Integer> httpsPortRef = new AtomicReference<>(null);
				AtomicReference<Integer> sshPortRef = new AtomicReference<>(null);
				kubectl.execute(new LineConsumer() {

					@Override
					public void consume(String line) {
						String protocol = StringUtils.substringBefore(line, " ");
						int port = Integer.parseInt(StringUtils.substringAfter(line, " "));
						if (protocol.equals("http"))
							httpPortRef.set(port);
						else if (protocol.equals("https"))
							httpsPortRef.set(port);
						else if (protocol.equals("ssh"))
							sshPortRef.set(port);
					}
					
				}, new LineConsumer() {

					@Override
					public void consume(String line) {
						logger.warn(line);
					}
					
				}).checkReturnCode();
				
				Integer sshPort = sshPortRef.get();
				Integer httpPort = httpPortRef.get();
				Integer httpsPort = httpsPortRef.get();

				if (ssh) {
					if (sshPort != null) 
						serverUrl = buildServerUrl(externalIp, "ssh", sshPort);
				} else if (httpsPort != null) {
					serverUrl = buildServerUrl(externalIp, "https", httpsPort);
				} else {
					serverUrl = buildServerUrl(externalIp, "http", httpPort);
				}
				
				if (serverUrl == null) {
					String httpPortEnv = System.getenv("ONEDEV_SERVICE_PORT_HTTP");
					String httpsPortEnv = System.getenv("ONEDEV_SERVICE_PORT_HTTPS");
					String sshPortEnv = System.getenv("ONEDEV_SERVICE_PORT_SSH");
					
					httpPort = httpPortEnv!=null?Integer.valueOf(httpPortEnv):null;
					httpsPort = httpsPortEnv!=null?Integer.valueOf(httpsPortEnv):null;
					sshPort = sshPortEnv!=null?Integer.valueOf(sshPortEnv):null;
					
					if (ssh) {
						if (sshPort != null) 
							serverUrl = buildServerUrl(externalIp, "ssh", sshPort);
					} else if (httpsPort != null) {
						serverUrl = buildServerUrl(externalIp, "https", httpsPort);
					} else {
						serverUrl = buildServerUrl(externalIp, "http", httpPort);
					}
				}
			} 			
		} 
		
		if (serverUrl == null) {
			String host;
			if (Bootstrap.isInDocker()) {
				host = "localhost";
			} else try {
				if (SystemUtils.IS_OS_MAC_OSX) {
					try (Socket socket = new Socket()) {
						socket.connect(new InetSocketAddress("microsoft.com", 80));
						host = StringUtils.stripStart(socket.getLocalAddress().toString(), "/");					
					} 
				} else {
					try (DatagramSocket socket = new DatagramSocket()) {
						socket.connect(InetAddress.getByName("8.8.8.8"), 10002);
						host = socket.getLocalAddress().getHostAddress();
					} 
				}
			} catch (Exception e) {
				try {
					host = InetAddress.getLocalHost().getHostName();
				} catch (UnknownHostException e2) {
					host = "localhost";
				}
			}
			
			ServerConfig serverConfig = serverConfigProvider.get();
			if (ssh) 
				serverUrl = buildServerUrl(host, "ssh", serverConfig.getSshPort());
			else if (serverConfig.getHttpsPort() != 0) 
                serverUrl = buildServerUrl(host, "https", serverConfig.getHttpsPort());
			else 
                serverUrl = buildServerUrl(host, "http", serverConfig.getHttpPort());
		}
		
		return serverUrl;
	}
	
	private Url buildServerUrl(String host, String protocol, int port) {
        Url serverUrl = new Url(Charset.forName("UTF8"));

        serverUrl.setHost(host);
        serverUrl.setProtocol(protocol);
        if (!protocol.equals("ssh") || port != 22)
        	serverUrl.setPort(port);
       
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
		
		if (Bootstrap.command == null) {
			taskScheduler.stop();
			jettyLauncherProvider.get().stop();
		}
		executorService.shutdown();
	}
		
	public Object writeReplace() throws ObjectStreamException {
		return new ManagedSerializedForm(OneDev.class);
	}	
	
	public String getDocRoot() {
		Version version = new Version(AppLoader.getProduct().getVersion());
		return "https://code.onedev.io/projects/onedev-manual/blob/" + version.getMajor() + "." + version.getMinor();
	}
	
}
