package io.onedev.server;

import java.io.IOException;
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
import io.onedev.server.git.ssh.SimpleGitSshServer;
import io.onedev.server.maintenance.DataManager;
import io.onedev.server.model.support.administration.SystemSetting;
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

    private SimpleGitSshServer simpleGitSshServer;
	
	@Inject
	public OneDev(JettyRunner jettyRunner, PersistManager persistManager, TaskScheduler taskScheduler,
			SessionManager sessionManager, ServerConfig serverConfig, DataManager dataManager, 
			SettingManager configManager, ExecutorService executorService, 
			ListenerRegistry listenerRegistry, SimpleGitSshServer simpleGitSshServer) {
		this.jettyRunner = jettyRunner;
		this.persistManager = persistManager;
		this.taskScheduler = taskScheduler;
		this.sessionManager = sessionManager;
		this.configManager = configManager;
		this.dataManager = dataManager;
		this.serverConfig = serverConfig;
		this.executorService = executorService;
		this.listenerRegistry = listenerRegistry;
        this.simpleGitSshServer = simpleGitSshServer;
		
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
			logger.warn("Please set up the server at " 
			            + guessServerUrl().toString(StringMode.FULL));
			initStage = new InitStage("Server Setup", manualConfigs);
			
			initStage.waitForFinish();
		}
		
		//TODO: provide better config support for Git server its certificates
		try {
            simpleGitSshServer.start();
        } catch (IOException e) {
            e.printStackTrace();
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
		SystemSetting systemSetting = configManager.getSystemSetting();
        logger.info("Server is ready at " + systemSetting.getServerUrl() + ".");
		logger.info("Git server started at " + systemSetting.getServerSshUrl());
		initStage = null;
	}

	public Url guessServerUrl() {
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
					throw new RuntimeException(e2);
				}
			}
			
			if (serverConfig.getHttpsPort() != 0)
                serverUrl = buildServerUrl(host, null, Integer.toString(serverConfig.getHttpsPort()));
            else 
                serverUrl = buildServerUrl(host, Integer.toString(serverConfig.getHttpPort()), null);
			
		}
		
		return serverUrl;
	}
	
	private Url buildServerUrl(String host, @Nullable String httpPort, @Nullable String httpsPort) {
        Url serverUrl = new Url(Charset.forName("UTF8"));
        boolean haveHttpPort = httpPort != null;
        boolean haveHttpsPort = httpsPort != null;

        serverUrl.setHost(host);
        serverUrl.setProtocol(haveHttpsPort ? "https" : "http");
        
        //Url class already strips out default ports (80, 443) in its toString method, so we don't need to check it. 
        if (haveHttpsPort || haveHttpPort ) {
            serverUrl.setPort(haveHttpsPort ? Integer.parseInt(httpsPort) : Integer.parseInt(httpPort));
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
		
		try {
            simpleGitSshServer.stop();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
	}
		
	public Object writeReplace() throws ObjectStreamException {
		return new ManagedSerializedForm(OneDev.class);
	}	
	
	public String getDocRoot() {
		return "https://code.onedev.io/projects/onedev-manual/blob/master";
	}
	
}
