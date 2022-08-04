package io.onedev.server;

import java.io.ObjectStreamException;
import java.io.Serializable;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicReference;

import javax.annotation.Nullable;
import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;
import org.apache.wicket.request.Url;
import org.eclipse.jgit.util.FS.FileStoreAttributes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Provider;

import io.onedev.commons.bootstrap.Bootstrap;
import io.onedev.commons.loader.AbstractPlugin;
import io.onedev.commons.loader.AppLoader;
import io.onedev.commons.loader.ListenerRegistry;
import io.onedev.commons.loader.ManagedSerializedForm;
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
import io.onedev.server.util.UrlUtils;
import io.onedev.server.util.Version;
import io.onedev.server.util.init.InitStage;
import io.onedev.server.util.init.ManualConfig;
import io.onedev.server.util.jetty.JettyLauncher;
import io.onedev.server.util.schedule.TaskScheduler;

public class OneDev extends AbstractPlugin implements Serializable {

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
	
	private final Date bootDate = new Date();
	
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
			if (getIngressUrl() != null)
				logger.warn("Please set up the server at " + getIngressUrl());
			else
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

	@Nullable
	public String getIngressUrl() {
		String ingressHost = System.getenv("ingress_host");
		if (ingressHost != null) {
			boolean ingressTLS = Boolean.parseBoolean(System.getenv("ingress_tls"));
			if (ingressTLS)
				return UrlUtils.toString(buildServerUrl(ingressHost, "https", 443));
			else
				return UrlUtils.toString(buildServerUrl(ingressHost, "http", 80));
		} else {
			return null;
		}
	}
	
	public static String getK8sService() {
		return System.getenv("k8s_service");
	}
	
	public String guessServerUrl() {
	    Url serverUrl = null;
	    
		String k8sService = getK8sService();
		if (k8sService != null) { // we are running inside Kubernetes  
			Commandline kubectl = new Commandline("kubectl");
			kubectl.addArgs("get", "service", k8sService, "-o", 
					"jsonpath={.status.loadBalancer.ingress[0].ip}");
			AtomicReference<String> externalIpRef = new AtomicReference<>(null);
			kubectl.execute(new LineConsumer() {

				@Override
				public void consume(String line) {
					if (StringUtils.isNotBlank(line))
						externalIpRef.set(line);
				}
				
			}, new LineConsumer() {

				@Override
				public void consume(String line) {
					logger.warn(line);
				}
				
			}).checkReturnCode();
			
			String externalIp = externalIpRef.get();
			
			if (externalIp != null) 
				serverUrl = buildServerUrl(externalIp, "http", 80);
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
			if (serverConfig.getHttpsPort() != 0) 
                serverUrl = buildServerUrl(host, "https", serverConfig.getHttpsPort());
			else 
                serverUrl = buildServerUrl(host, "http", serverConfig.getHttpPort());
		}
		
		return UrlUtils.toString(serverUrl);
	}
	
	private Url buildServerUrl(String host, String protocol, int port) {
        Url serverUrl = new Url(Charset.forName("UTF8"));

        serverUrl.setHost(host);
        serverUrl.setProtocol(protocol);
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

	public Date getBootDate() {
		return bootDate;
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
		return "https://code.onedev.io/projects/162/blob/" + version.getMajor() + "." + version.getMinor();
	}
	
}
