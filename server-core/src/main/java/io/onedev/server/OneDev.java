package io.onedev.server;

import com.google.inject.Provider;
import com.hazelcast.core.HazelcastInstance;
import io.onedev.commons.bootstrap.Bootstrap;
import io.onedev.commons.loader.AbstractPlugin;
import io.onedev.commons.loader.AppLoader;
import io.onedev.commons.loader.ManagedSerializedForm;
import io.onedev.commons.utils.FileUtils;
import io.onedev.commons.utils.command.Commandline;
import io.onedev.commons.utils.command.LineConsumer;
import io.onedev.k8shelper.KubernetesHelper;
import io.onedev.server.cluster.ClusterManager;
import io.onedev.server.event.ListenerRegistry;
import io.onedev.server.event.system.SystemStarted;
import io.onedev.server.event.system.SystemStarting;
import io.onedev.server.event.system.SystemStopped;
import io.onedev.server.event.system.SystemStopping;
import io.onedev.server.exception.ServerNotReadyException;
import io.onedev.server.jetty.JettyLauncher;
import io.onedev.server.persistence.IdManager;
import io.onedev.server.persistence.PersistenceManager;
import io.onedev.server.persistence.SessionFactoryManager;
import io.onedev.server.persistence.SessionManager;
import io.onedev.server.persistence.annotation.Sessional;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.util.UrlUtils;
import io.onedev.server.util.init.InitStage;
import io.onedev.server.util.schedule.TaskScheduler;
import org.apache.commons.lang3.StringUtils;
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
import java.net.ServerSocket;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.Date;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicReference;

import static io.onedev.commons.utils.FileUtils.loadProperties;
import static io.onedev.k8shelper.KubernetesHelper.BEARER;
import static io.onedev.server.persistence.PersistenceUtils.callWithTransaction;
import static javax.ws.rs.core.HttpHeaders.AUTHORIZATION;

public class OneDev extends AbstractPlugin implements Serializable {
	
	private static final Logger logger = LoggerFactory.getLogger(OneDev.class);

	private final Provider<JettyLauncher> jettyLauncherProvider;
		
	private final SessionManager sessionManager;
	
	private final PersistenceManager persistenceManager;
	
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
				  PersistenceManager persistenceManager, ExecutorService executorService,
				  ListenerRegistry listenerRegistry, ClusterManager clusterManager,
				  IdManager idManager, SessionFactoryManager sessionFactoryManager) {
		this.jettyLauncherProvider = jettyLauncherProvider;
		this.taskScheduler = taskScheduler;
		this.sessionManager = sessionManager;
		this.persistenceManager = persistenceManager;
		this.serverConfigProvider = serverConfigProvider;
		this.executorService = executorService;
		this.listenerRegistry = listenerRegistry;
		this.clusterManager = clusterManager;
		this.idManager = idManager;
		this.sessionFactoryManager = sessionFactoryManager;

		initStage = new InitStage("Server is Starting...");
	}
	
	@Override
	public void start() {
		SecurityUtils.bindAsSystem();
		
		System.setProperty("hsqldb.reconfig_logging", "false");
		System.setProperty("hsqldb.method_class_names", "java.lang.Math");
		
		sessionFactoryManager.start();

		try (var conn = persistenceManager.openConnection()) {
			callWithTransaction(conn, () -> {
				persistenceManager.populateDatabase(conn);
				return null;
			});
		} catch (SQLException e) {
			throw new RuntimeException(e);
		};
		
		clusterManager.start();
		idManager.init();

		// restart session factory to pick up Hazelcast 2nd level cache 
		sessionFactoryManager.stop();
		sessionFactoryManager.start();
		
		sessionManager.run(() -> listenerRegistry.post(new SystemStarting()));
		jettyLauncherProvider.get().start();

		HazelcastInstance hazelcastInstance = clusterManager.getHazelcastInstance();
		var dataChecked = hazelcastInstance.getCPSubsystem().getAtomicLong("dataInited");
		clusterManager.init(dataChecked, () -> {
			var manualConfigs = persistenceManager.checkData();
			if (!manualConfigs.isEmpty()) {
				if (getIngressUrl() != null)
					logger.warn("Please set up the server at " + getIngressUrl());
				else
					logger.warn("Please set up the server at " + guessServerUrl());
				initStage = new InitStage("Server Setup", manualConfigs);
				
				initStage.waitForFinish();
			}
			return 1L;
		});
		
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
		SecurityUtils.bindAsSystem();
		initStage = null;
		listenerRegistry.post(new SystemStarted());
        logger.info("Server is ready at " + guessServerUrl() + ".");
	}

	@Override
	public void preStop() {
		SecurityUtils.bindAsSystem();
		try {
			sessionManager.run(() -> listenerRegistry.post(new SystemStopping()));
		} catch (ServerNotReadyException ignore) {
		}
	}

	@Override
	public void stop() {
		SecurityUtils.bindAsSystem();

		try {
			taskScheduler.stop();

			jettyLauncherProvider.get().stop();
			sessionManager.run(() -> listenerRegistry.post(new SystemStopped()));

			// stop cluster manager first as it depends on metadata of session factory
			clusterManager.stop();
			sessionFactoryManager.stop();
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
		Properties props = loadProperties(new File(installDir, "conf/server.properties"));
		int sshPort = Integer.parseInt(props.get("ssh_port").toString());
		try (ServerSocket ignored = new ServerSocket(sshPort)) {
			return false;
		} catch (IOException e) {
			if (e.getMessage() != null && e.getMessage().contains("Address already in use"))
				return true;
			else
				throw new RuntimeException(e);
		}		
	}

	public static File getAssetsDir() {
		return new File(Bootstrap.getSiteDir(), "assets");
	}
	
}
