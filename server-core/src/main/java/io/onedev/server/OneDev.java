package io.onedev.server;

import com.google.inject.Provider;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.cp.IAtomicLong;
import io.onedev.commons.loader.AbstractPlugin;
import io.onedev.commons.loader.AppLoader;
import io.onedev.commons.loader.ManagedSerializedForm;
import io.onedev.commons.utils.FileUtils;
import io.onedev.commons.utils.command.Commandline;
import io.onedev.commons.utils.command.LineConsumer;
import io.onedev.server.cluster.ClusterManager;
import io.onedev.server.entitymanager.SettingManager;
import io.onedev.server.event.ListenerRegistry;
import io.onedev.server.event.system.SystemStarted;
import io.onedev.server.event.system.SystemStarting;
import io.onedev.server.event.system.SystemStopped;
import io.onedev.server.event.system.SystemStopping;
import io.onedev.server.exception.SystemNotReadyException;
import io.onedev.server.jetty.JettyLauncher;
import io.onedev.server.persistence.*;
import io.onedev.server.persistence.annotation.Sessional;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.util.UrlUtils;
import io.onedev.server.util.init.InitStage;
import io.onedev.server.util.init.ManualConfig;
import io.onedev.server.util.schedule.TaskScheduler;
import org.apache.commons.lang.math.RandomUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.wicket.request.Url;
import org.eclipse.jgit.util.FS.FileStoreAttributes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.net.ServerSocket;
import java.nio.charset.Charset;
import java.sql.Connection;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicReference;

public class OneDev extends AbstractPlugin implements Serializable {

	private static final Logger logger = LoggerFactory.getLogger(OneDev.class);
	
	private final Provider<JettyLauncher> jettyLauncherProvider;
		
	private final SessionManager sessionManager;
	
	private final DataManager dataManager;
	
	private final SettingManager settingManager;
			
	private final Provider<ServerConfig> serverConfigProvider;
	
	private final ListenerRegistry listenerRegistry;
	
	private final TaskScheduler taskScheduler;
	
	private final ExecutorService executorService;
	
	private final ClusterManager clusterManager;
	
	private final IdManager idManager;
	
	private final SessionFactoryManager sessionFactoryManager;
	
	private final Date bootDate = new Date();
	
	private volatile InitStage initStage;
	
	// Some are injected via provider as instantiation might encounter problem during upgrade 
	@Inject
	public OneDev(Provider<JettyLauncher> jettyLauncherProvider, TaskScheduler taskScheduler, 
			SessionManager sessionManager, Provider<ServerConfig> serverConfigProvider, 
			DataManager dataManager, ExecutorService executorService, 
			SettingManager settingManager, ListenerRegistry listenerRegistry, 
			ClusterManager clusterManager, IdManager idManager, 
			SessionFactoryManager sessionFactoryManager) {
		this.jettyLauncherProvider = jettyLauncherProvider;
		this.taskScheduler = taskScheduler;
		this.sessionManager = sessionManager;
		this.dataManager = dataManager;
		this.serverConfigProvider = serverConfigProvider;
		this.executorService = executorService;
		this.listenerRegistry = listenerRegistry;
		this.clusterManager = clusterManager;
		this.idManager = idManager;
		this.settingManager = settingManager;
		this.sessionFactoryManager = sessionFactoryManager;

		initStage = new InitStage("Server is Starting...");
	}
	
	@Override
	public void start() {
		SecurityUtils.bindAsSystem();
		
		System.setProperty("hsqldb.reconfig_logging", "false");
		System.setProperty("hsqldb.method_class_names", "java.lang.Math");
		
		jettyLauncherProvider.get().start();
		taskScheduler.start();
		
		sessionFactoryManager.start();
		
		dataManager.callWithConnection(new ConnectionCallable<Void>() {

			@Override
			public Void call(Connection conn) {
				while (true) {
					try {
						dataManager.populateDatabase(conn);
						break;
					} catch (Exception e) {
						logger.warn("Error initializing data, will retry later...", e);
						try {
							Thread.sleep((RandomUtils.nextInt(5)+1)*1000L);
						} catch (InterruptedException e2) {
						}
					}
				}
				return null;
			}
			
		});
		
		clusterManager.start();
		idManager.init();

		// restart session factory to pick up Hazelcast 2nd level cache 
		sessionFactoryManager.stop();
		sessionFactoryManager.start();
		
		settingManager.init();

		HazelcastInstance hazelcastInstance = clusterManager.getHazelcastInstance();
		IAtomicLong dataChecked = hazelcastInstance.getCPSubsystem().getAtomicLong("dataInited");
		clusterManager.init(dataChecked, new Callable<Long>() {

			@Override
			public Long call() throws Exception {
				List<ManualConfig> manualConfigs = dataManager.checkData();
				if (!manualConfigs.isEmpty()) {
					if (getIngressUrl() != null)
						logger.warn("Please set up the server at " + getIngressUrl());
					else
						logger.warn("Please set up the server at " + guessServerUrl());
					initStage = new InitStage("Server Setup", manualConfigs);
					
					initStage.waitForFinish();
				}
				return 1L;
			}
			
		});
		
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
        logger.info("Server is ready at " + guessServerUrl() + ".");
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
			ServerConfig serverConfig = serverConfigProvider.get();
            serverUrl = buildServerUrl("localhost", "http", serverConfig.getHttpPort());
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

	@Override
	public void preStop() {
		SecurityUtils.bindAsSystem();
		
		try {
			sessionManager.run(new Runnable() {
				@Override
				public void run() {
					listenerRegistry.post(new SystemStopping());
				}

			});
		} catch (SystemNotReadyException ignore) {
		}
	}

	@Override
	public void stop() {
		SecurityUtils.bindAsSystem();

		try {
			sessionManager.run(new Runnable() {

				@Override
				public void run() {
					listenerRegistry.post(new SystemStopped());
				}

			});

			// stop cluster manager first as it depends on metadata of session factory
			clusterManager.stop();
			sessionFactoryManager.stop();
			taskScheduler.stop();
			executorService.shutdown();
			jettyLauncherProvider.get().stop();
		} catch (SystemNotReadyException ignore) {
		}
	}
	
	public Object writeReplace() throws ObjectStreamException {
		return new ManagedSerializedForm(OneDev.class);
	}	
	
	public static boolean isServerRunning(File installDir) {
		Properties props = FileUtils.loadProperties(new File(installDir, "conf/server.properties"));
		int sshPort = Integer.parseInt(props.get("ssh_port").toString());
		try (ServerSocket serverSocket = new ServerSocket(sshPort)) {
			return false;
		} catch (IOException e) {
			if (e.getMessage() != null && e.getMessage().contains("Address already in use"))
				return true;
			else
				throw new RuntimeException(e);
		}		
	}

}
