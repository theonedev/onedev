package io.onedev.server;

import com.google.inject.Provider;
import com.hazelcast.core.HazelcastInstance;
import io.onedev.commons.bootstrap.Bootstrap;
import io.onedev.commons.loader.AbstractPlugin;
import io.onedev.commons.loader.AppLoader;
import io.onedev.commons.loader.ManagedSerializedForm;
import io.onedev.commons.utils.FileUtils;
import io.onedev.k8shelper.KubernetesHelper;
import io.onedev.server.cluster.ClusterManager;
import io.onedev.server.data.DataManager;
import io.onedev.server.event.ListenerRegistry;
import io.onedev.server.event.system.SystemStarted;
import io.onedev.server.event.system.SystemStarting;
import io.onedev.server.event.system.SystemStopped;
import io.onedev.server.event.system.SystemStopping;
import io.onedev.server.exception.ServerNotReadyException;
import io.onedev.server.jetty.JettyLauncher;
import io.onedev.server.persistence.IdManager;
import io.onedev.server.persistence.SessionFactoryManager;
import io.onedev.server.persistence.SessionManager;
import io.onedev.server.persistence.annotation.Sessional;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.util.UrlUtils;
import io.onedev.server.util.init.InitStage;
import io.onedev.server.util.init.ManualConfig;
import io.onedev.server.taskschedule.TaskScheduler;
import org.apache.wicket.request.Url;
import org.eclipse.jgit.util.FS.FileStoreAttributes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;
import java.io.*;
import java.lang.reflect.Method;
import java.net.ServerSocket;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;

import static io.onedev.k8shelper.KubernetesHelper.BEARER;
import static io.onedev.server.persistence.PersistenceUtils.callWithTransaction;
import static javax.ws.rs.core.HttpHeaders.AUTHORIZATION;

public class OneDev extends AbstractPlugin implements Serializable, Runnable {
	
	private static final Logger logger = LoggerFactory.getLogger(OneDev.class);
	
	private final Provider<JettyLauncher> jettyLauncherProvider;
		
	private final SessionManager sessionManager;
	
	private final DataManager dataManager;
	
	private final Provider<ServerConfig> serverConfigProvider;
	
	private final ListenerRegistry listenerRegistry;
	
	private final TaskScheduler taskScheduler;
	
	private final ExecutorService executorService;
	
	private final ClusterManager clusterManager;
	
	private final IdManager idManager;
	
	private final SessionFactoryManager sessionFactoryManager;
	
	private final Date bootDate = new Date();
	
	private volatile InitStage initStage;
	
	private Class<?> wrapperManagerClass;	
	
	private volatile Thread thread;
	
	// Some are injected via provider as instantiation might encounter problem during upgrade 
	@Inject
	public OneDev(Provider<JettyLauncher> jettyLauncherProvider, TaskScheduler taskScheduler,
                  SessionManager sessionManager, Provider<ServerConfig> serverConfigProvider,
                  DataManager dataManager, ExecutorService executorService,
                  ListenerRegistry listenerRegistry, ClusterManager clusterManager,
                  IdManager idManager, SessionFactoryManager sessionFactoryManager) {
		this.jettyLauncherProvider = jettyLauncherProvider;
		this.taskScheduler = taskScheduler;
		this.sessionManager = sessionManager;
		this.dataManager = dataManager;
		this.serverConfigProvider = serverConfigProvider;
		this.executorService = executorService;
		this.listenerRegistry = listenerRegistry;
		this.clusterManager = clusterManager;
		this.idManager = idManager;
		this.sessionFactoryManager = sessionFactoryManager;
		
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

		clusterManager.start();
		sessionFactoryManager.start();
		
		var databasePopulated = clusterManager.getHazelcastInstance().getCPSubsystem().getAtomicLong("databasePopulated");
		// Do not use database lock as schema update will commit transaction immediately 
		// in MySQL 
		clusterManager.init(databasePopulated, () -> {
			try (var conn = dataManager.openConnection()) {
				callWithTransaction(conn, () -> {
					dataManager.populateDatabase(conn);
					return null;
				});
			} catch (SQLException e) {
				throw new RuntimeException(e);
			};
			return 1L;
		});
		
		idManager.init();

		sessionManager.run(() -> listenerRegistry.post(new SystemStarting()));
		jettyLauncherProvider.get().start();

		var manualConfigs = checkData();
		if (!manualConfigs.isEmpty()) {
			if (getIngressUrl() != null)
				logger.warn("Please set up the server at " + getIngressUrl());
			else
				logger.warn("Please set up the server at " + guessServerUrl());
			initStage = new InitStage("Server Setup", manualConfigs);
			var localServer = clusterManager.getLocalServerAddress();
			while (true) {
				if (maintenanceFile.exists()) {
					logger.info("Maintenance requested, trying to stop all servers...");
					clusterManager.submitToAllServers(() -> {
						if (!localServer.equals(clusterManager.getLocalServerAddress()))
							restart();
						return null;
					});
					while (thread != null && clusterManager.getServerAddresses().size() != 1) {
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
		
		var leadServer = clusterManager.getLeaderServerAddress();
		if (!leadServer.equals(clusterManager.getLocalServerAddress())) {
			logger.info("Syncing assets...");
			Client client = ClientBuilder.newClient();
			try {
				String fromServerUrl = clusterManager.getServerUrl(leadServer);
				WebTarget target = client.target(fromServerUrl).path("/~api/cluster/assets");
				Invocation.Builder builder = target.request();
				builder.header(AUTHORIZATION,
						BEARER + " " + clusterManager.getCredential());

				try (Response response = builder.get()) {
					KubernetesHelper.checkStatus(response);
					try (InputStream is = response.readEntity(InputStream.class)) {							
						FileUtils.untar(is, getAssetsDir(), false);
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
		clusterManager.postStart();
		thread.start();
		logger.info("Server is ready at " + guessServerUrl());
	}

	@Override
	public void preStop() {
		thread = null;
		clusterManager.preStop();
		SecurityUtils.bindAsSystem();
		try {
			sessionManager.run(() -> listenerRegistry.post(new SystemStopping()));
		} catch (ServerNotReadyException ignore) {
		}
	}
	
	private List<ManualConfig> checkData() {
		HazelcastInstance hazelcastInstance = clusterManager.getHazelcastInstance();
		var lock = hazelcastInstance.getCPSubsystem().getLock("checkData");
		lock.lock();
		try {
			return dataManager.checkData();
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
			sessionManager.run(() -> listenerRegistry.post(new SystemStopped()));

			sessionFactoryManager.stop();
			clusterManager.stop();
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
			ServerConfig serverConfig = serverConfigProvider.get();
			var serverUrl = buildServerUrl("localhost", "http", serverConfig.getHttpPort());
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
		var localServer = clusterManager.getLocalServerAddress();
		var maintenanceFile = getMaintenanceFile(Bootstrap.installDir);
		while (thread != null) {
			if (maintenanceFile.exists()) {
				logger.info("Maintenance requested, trying to stop all servers...");
				clusterManager.submitToAllServers(() -> {
					if (!localServer.equals(clusterManager.getLocalServerAddress())) 
						restart();
					return null;
				});
				while (thread != null && clusterManager.getServerAddresses().size() != 1) {
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
