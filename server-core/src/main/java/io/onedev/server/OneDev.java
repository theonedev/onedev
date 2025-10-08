package io.onedev.server;

import static io.onedev.k8shelper.KubernetesHelper.BEARER;
import static io.onedev.k8shelper.KubernetesHelper.checkStatus;
import static io.onedev.server.persistence.PersistenceUtils.callWithTransaction;
import static javax.ws.rs.core.HttpHeaders.AUTHORIZATION;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;

import org.jspecify.annotations.Nullable;
import javax.inject.Inject;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;

import org.apache.wicket.request.Url;
import org.eclipse.jgit.util.FS.FileStoreAttributes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Provider;
import com.hazelcast.core.HazelcastInstance;

import io.onedev.commons.bootstrap.Bootstrap;
import io.onedev.commons.loader.AbstractPlugin;
import io.onedev.commons.loader.AppLoader;
import io.onedev.commons.loader.ManagedSerializedForm;
import io.onedev.commons.utils.FileUtils;
import io.onedev.commons.utils.TarUtils;
import io.onedev.server.cluster.ClusterService;
import io.onedev.server.data.DataService;
import io.onedev.server.service.SettingService;
import io.onedev.server.event.ListenerRegistry;
import io.onedev.server.event.system.SystemStarted;
import io.onedev.server.event.system.SystemStarting;
import io.onedev.server.event.system.SystemStopped;
import io.onedev.server.event.system.SystemStopping;
import io.onedev.server.exception.ServerNotReadyException;
import io.onedev.server.jetty.JettyService;
import io.onedev.server.model.support.administration.SystemSetting;
import io.onedev.server.persistence.IdService;
import io.onedev.server.persistence.SessionFactoryService;
import io.onedev.server.persistence.SessionService;
import io.onedev.server.persistence.annotation.Sessional;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.taskschedule.TaskScheduler;
import io.onedev.server.util.UrlUtils;
import io.onedev.server.util.init.InitStage;
import io.onedev.server.util.init.ManualConfig;

public class OneDev extends AbstractPlugin implements Serializable, Runnable {
	
	private static final Logger logger = LoggerFactory.getLogger(OneDev.class);
	
	private final Provider<JettyService> jettyLauncherProvider;
		
	private final SessionService sessionService;
	
	private final DataService dataService;
	
	private final Provider<ServerConfig> serverConfigProvider;
	
	private final ListenerRegistry listenerRegistry;
	
	private final TaskScheduler taskScheduler;
	
	private final ExecutorService executorService;
	
	private final ClusterService clusterService;
	
	private final SettingService settingService;
	
	private final IdService idService;
	
	private final SessionFactoryService sessionFactoryService;
	
	private final Date bootDate = new Date();
	
	private volatile InitStage initStage;
	
	private Class<?> wrapperManagerClass;	
	
	private volatile Thread thread;
	
	// Some are injected via provider as instantiation might encounter problem during upgrade 
	@Inject
	public OneDev(Provider<JettyService> jettyLauncherProvider, TaskScheduler taskScheduler,
                  SessionService sessionService, Provider<ServerConfig> serverConfigProvider,
                  DataService dataService, ExecutorService executorService,
                  ListenerRegistry listenerRegistry, ClusterService clusterService,
                  IdService idService, SessionFactoryService sessionFactoryService,
                  SettingService settingService) {
		this.jettyLauncherProvider = jettyLauncherProvider;
		this.taskScheduler = taskScheduler;
		this.sessionService = sessionService;
		this.dataService = dataService;
		this.serverConfigProvider = serverConfigProvider;
		this.executorService = executorService;
		this.listenerRegistry = listenerRegistry;
		this.clusterService = clusterService;
		this.idService = idService;
		this.sessionFactoryService = sessionFactoryService;
		this.settingService = settingService;
		
		try {
			wrapperManagerClass = Class.forName("org.tanukisoftware.wrapper.WrapperManager");
		} catch (ClassNotFoundException e) {
		}
		thread = new Thread(this);

		initStage = new InitStage("Server is Starting...");
	}
	
	@Override
	public void start() {
		var maintenanceFile = getMaintenanceFile(Bootstrap.installDir);
		while (maintenanceFile.exists()) {
			logger.info("Maintenance in progress, waiting...");
			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			}
		}
		
		SecurityUtils.bindAsSystem();
		
		System.setProperty("hsqldb.reconfig_logging", "false");
		System.setProperty("hsqldb.method_class_names", "java.lang.Math");

		clusterService.start();
		sessionFactoryService.start();
		
		var databasePopulated = clusterService.getHazelcastInstance().getCPSubsystem().getAtomicLong("databasePopulated");
		// Do not use database lock as schema update will commit transaction immediately 
		// in MySQL 
		clusterService.initWithLead(databasePopulated, () -> {
			try (var conn = dataService.openConnection()) {
				callWithTransaction(conn, () -> {
					dataService.populateDatabase(conn);
					return null;
				});
			} catch (SQLException e) {
				throw new RuntimeException(e);
			};
			return 1L;
		});
		
		idService.init();

		sessionService.run(() -> listenerRegistry.post(new SystemStarting()));
		jettyLauncherProvider.get().start();

		var manualConfigs = checkData();
		if (!manualConfigs.isEmpty()) {
			if (getIngressUrl() != null)
				logger.warn("Please set up the server at " + getIngressUrl());
			else
				logger.warn("Please set up the server at " + guessServerUrl());
			initStage = new InitStage("Server Setup", manualConfigs);
			var localServer = clusterService.getLocalServerAddress();
			while (true) {
				if (maintenanceFile.exists()) {
					logger.info("Maintenance requested, trying to stop all servers...");
					clusterService.submitToAllServers(() -> {
						if (!localServer.equals(clusterService.getLocalServerAddress()))
							restart();
						return null;
					});
					while (thread != null && clusterService.getServerAddresses().size() != 1) {
						try {
							Thread.sleep(1000);
						} catch (InterruptedException ignored) {
						}
					}
					restart();
					return;
				}
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					throw new RuntimeException(e);
				}
				if (thread == null)
					return;
				
				manualConfigs = checkData();
				if (manualConfigs.isEmpty()) {
					initStage = new InitStage("Please wait...");
					break;
				} else {
					initStage = new InitStage("Server Setup", manualConfigs);
				}
			}
		}
		
		var leadServer = clusterService.getLeaderServerAddress();
		if (!leadServer.equals(clusterService.getLocalServerAddress())) {
			logger.info("Syncing assets...");
			Client client = ClientBuilder.newClient();
			try {
				String fromServerUrl = clusterService.getServerUrl(leadServer);
				WebTarget target = client.target(fromServerUrl).path("/~api/cluster/assets");
				Invocation.Builder builder = target.request();
				builder.header(AUTHORIZATION,
						BEARER + " " + clusterService.getCredential());

				try (Response response = builder.get()) {
					checkStatus(response);
					try (InputStream is = response.readEntity(InputStream.class)) {							
						TarUtils.untar(is, getAssetsDir(), false);
					} catch (IOException e) {
						throw new RuntimeException(e);
					}
				}
			} finally {
				client.close();
			}
		}
		
		// workaround for issue https://bugs.eclipse.org/bugs/show_bug.cgi?id=566170
		FileStoreAttributes.setBackground(true);
		taskScheduler.start();
	}
	
	@Sessional
	@Override
	public void postStart() {
		if (thread == null) 
			return;
		SecurityUtils.bindAsSystem();
		initStage = null;
		listenerRegistry.post(new SystemStarted());
		clusterService.postStart();
		thread.start();

		SystemSetting systemSetting = settingService.getSystemSetting();
		logger.info("Server is ready at " + systemSetting.getServerUrl() + ".");
	}

	@Override
	public void preStop() {
		thread = null;
		clusterService.preStop();
		SecurityUtils.bindAsSystem();
		try {
			sessionService.run(() -> listenerRegistry.post(new SystemStopping()));
		} catch (ServerNotReadyException ignore) {
		}
	}
	
	private List<ManualConfig> checkData() {
		HazelcastInstance hazelcastInstance = clusterService.getHazelcastInstance();
		var lock = hazelcastInstance.getCPSubsystem().getLock("checkData");
		lock.lock();
		try {
			return dataService.checkData();
		} finally {
			lock.unlock();
		}
	}

	@Override
	public void stop() {
		SecurityUtils.bindAsSystem();

		try {
			taskScheduler.stop();

			jettyLauncherProvider.get().stop();
			sessionService.run(() -> listenerRegistry.post(new SystemStopped()));

			sessionFactoryService.stop();
			clusterService.stop();
			executorService.shutdown();
		} catch (ServerNotReadyException ignore) {
		}
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
		String serviceHost = System.getenv("ONEDEV_SERVICE_HOST");
		if (serviceHost != null) {
			return "http://" + serviceHost;
		} else {
			String hostName;
			if (Bootstrap.isInDocker()) {
				hostName = "localhost";
			} else {
				try {
					hostName = InetAddress.getLocalHost().getHostName();
				} catch (UnknownHostException e) {
					hostName = "localhost";
				}
			}
			ServerConfig serverConfig = serverConfigProvider.get();
			var serverUrl = buildServerUrl(hostName, "http", serverConfig.getHttpPort());
			return UrlUtils.toString(serverUrl);
		}
	}
	
	private Url buildServerUrl(String host, String protocol, int port) {
        Url serverUrl = new Url(StandardCharsets.UTF_8);

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

	public static <T> T getInstance(Class<T> type, Class<? extends Annotation> annotationClass) {
		return AppLoader.getInstance(type, annotationClass);
	}

	public static <T> Set<T> getExtensions(Class<T> extensionPoint) {
		return AppLoader.getExtensions(extensionPoint);
	}

	public Date getBootDate() {
		return bootDate;
	}

	public Object writeReplace() throws ObjectStreamException {
		return new ManagedSerializedForm(OneDev.class);
	}	
	
	public static boolean isServerRunning(File installDir) {
		var serverConfig = new ServerConfig(installDir);
		try (ServerSocket ignored = new ServerSocket(serverConfig.getClusterPort())) {
			return false;
		} catch (IOException e) {
			if (e.getMessage() != null && e.getMessage().contains("Address already in use"))
				return true;
			else
				throw new RuntimeException(e);
		}		
	}

	public static File getIndexDir() {
		File indexDir = new File(Bootstrap.getSiteDir(), "index");
		FileUtils.createDir(indexDir);
		return indexDir;
	}
	
	public static File getAssetsDir() {
		return new File(Bootstrap.getSiteDir(), "assets");
	}
	
	public static File getMaintenanceFile(File installDir) {
		return new File(installDir, "maintenance");
	}
	
	private void restart() {
		if (wrapperManagerClass != null) {
			try {
				Method method = wrapperManagerClass.getDeclaredMethod("restartAndReturn");
				method.invoke(null, new Object[0]);
			} catch (Exception e) {
				logger.error("Error restarting server", e);
			}
		} else {
			logger.warn("Restart request ignored as there is no wrapper manager available");
		}
	}

	@Override
	public void run() {
		var localServer = clusterService.getLocalServerAddress();
		var maintenanceFile = getMaintenanceFile(Bootstrap.installDir);
		while (thread != null) {
			if (maintenanceFile.exists()) {
				logger.info("Maintenance requested, trying to stop all servers...");
				clusterService.submitToAllServers(() -> {
					if (!localServer.equals(clusterService.getLocalServerAddress())) 
						restart();
					return null;
				});
				while (thread != null && clusterService.getServerAddresses().size() != 1) {
					try {
						Thread.sleep(1000);
					} catch (InterruptedException ignored) {
					}
				}
				restart();
				break;
			} 
			try {
				Thread.sleep(1000);
			} catch (InterruptedException ignored) {
			}
		}
	}
}
