package io.onedev.server.jetty;

import java.io.ObjectStreamException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import jakarta.inject.Inject;
import jakarta.inject.Provider;
import jakarta.inject.Singleton;
import jakarta.servlet.DispatcherType;
import jakarta.servlet.http.HttpServletResponse;

import org.apache.tika.mime.MimeTypes;
import org.eclipse.jetty.http.HttpCookie.SameSite;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.compression.gzip.GzipCompression;
import org.eclipse.jetty.compression.server.CompressionConfig;
import org.eclipse.jetty.compression.server.CompressionHandler;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.session.DefaultSessionIdManager;
import org.eclipse.jetty.session.HouseKeeper;
import org.eclipse.jetty.session.SessionDataStoreFactory;
import org.eclipse.jetty.ee11.servlet.ErrorPageErrorHandler;
import org.eclipse.jetty.ee11.servlet.ServletContextHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.servlet.GuiceFilter;

import io.onedev.commons.bootstrap.Bootstrap;
import io.onedev.commons.loader.ManagedSerializedForm;
import io.onedev.commons.utils.ExceptionUtils;
import io.onedev.server.OneDev;
import io.onedev.server.cluster.ClusterRunnable;
import io.onedev.server.cluster.ClusterService;
import io.onedev.server.cluster.ClusterTask;
import io.onedev.server.event.Listen;
import io.onedev.server.event.entity.EntityPersisted;
import io.onedev.server.model.Setting;
import io.onedev.server.model.support.administration.SystemSetting;
import io.onedev.server.persistence.TransactionService;
import io.onedev.server.persistence.annotation.Transactional;
import io.onedev.server.service.SettingService;

@Singleton
public class DefaultJettyService implements JettyService, Serializable {

	private static final Logger logger = LoggerFactory.getLogger(DefaultJettyService.class);
	
	private static final int DEFAULT_SESSION_TIMEOUT = 1800;

	private static final int MAX_CONTENT_SIZE = 5000000;

	private static final int SESSION_SCAVENGE_INTERVAL = 60;

	@Inject
	private SessionDataStoreFactory sessionDataStoreFactory;
	
	private volatile Server server;
	
	private volatile ServletContextHandler servletContextHandler;

	@Inject
	private Provider<Set<ServerConfigurator>> serverConfiguratorsProvider;

	@Inject
	private Provider<Set<ServletConfigurator>> servletConfiguratorsProvider;

	@Inject
	private SettingService settingService;
	
	@Inject
	private TransactionService transactionService;
	
	@Inject
	private ClusterService clusterService;
	
	@Override
	public void start() {
		server = new Server();

        var sessionIdManager = new DefaultSessionIdManager(server);
        var houseKeeper = new HouseKeeper();				
        try {
			// Set the interval to clean up expired sessions
			houseKeeper.setIntervalSec(SESSION_SCAVENGE_INTERVAL);
		} catch (Exception e) {
			throw ExceptionUtils.unchecked(e);
		}
        sessionIdManager.setSessionHouseKeeper(houseKeeper);
        server.addBean(sessionIdManager, true);

		server.addBean(sessionDataStoreFactory);
		
        servletContextHandler = new ServletContextHandler(ServletContextHandler.SESSIONS);
        servletContextHandler.setMaxFormContentSize(MAX_CONTENT_SIZE);

        servletContextHandler.setClassLoader(OneDev.class.getClassLoader());
		org.eclipse.jetty.ee11.websocket.server.config.JettyWebSocketServletContainerInitializer.configure(servletContextHandler, null);
        
        servletContextHandler.setErrorHandler(new ErrorPageErrorHandler());
        servletContextHandler.addFilter(DisableTraceFilter.class, "/*", EnumSet.of(DispatcherType.REQUEST));
        
        servletContextHandler.getSessionHandler().setSessionIdPathParameterName(null);
        servletContextHandler.getSessionHandler().setSameSite(SameSite.LAX);  
        servletContextHandler.getSessionHandler().setHttpOnly(true);
		var sessionTimeout = DEFAULT_SESSION_TIMEOUT;
		if (settingService.getSystemSetting() != null && settingService.getSystemSetting().getSessionTimeout() != null) 
			sessionTimeout = settingService.getSystemSetting().getSessionTimeout() * 60;

		servletContextHandler.getSessionHandler().setMaxInactiveInterval(sessionTimeout);		

        /*
         * By default contributions is in reverse dependency order. We reverse the order so that 
         * servlet and filter contributions in dependency plugins comes first. 
         */
        List<ServletConfigurator> servletConfigurators = new ArrayList<>(servletConfiguratorsProvider.get());
        Collections.reverse(servletConfigurators);
        for (ServletConfigurator configurator: servletConfigurators) {
        	configurator.configure(servletContextHandler);
        }

        /*
         *  Add Guice filter as last filter in order to make sure that filters and servlets
         *  configured in Guice web module can be filtered correctly by filters added to 
         *  Jetty context directly.  
         */
        servletContextHandler.addFilter(GuiceFilter.class, "/*", EnumSet.of(DispatcherType.REQUEST));

		ErrorPageErrorHandler errorHandler = new ErrorPageErrorHandler();
		errorHandler.setBufferSize(64 * 1024);
	    errorHandler.addErrorPage(HttpServletResponse.SC_NOT_FOUND, "/~errors/404");
	    servletContextHandler.setErrorHandler(errorHandler);

		var handlers = new Handler.Sequence();
		handlers.addHandler(new ProbeHandler(() -> OneDev.getInstance().isReady()
				&& !OneDev.getInstance().isStopping()
				&& !OneDev.getMaintenanceFile(Bootstrap.installDir).exists()));
		handlers.addHandler(newCompressionHandler(servletContextHandler));
        server.setHandler(handlers);
        
        for (ServerConfigurator configurator: serverConfiguratorsProvider.get()) 
        	configurator.configure(server);
        
        if (Bootstrap.command == null) {
			try {
				server.start();
			} catch (Exception e) {
				throw ExceptionUtils.unchecked(e);
			}
		}
	}

	static CompressionHandler newCompressionHandler(Handler handler) {
		var compression = new CompressionHandler(handler);
		var gzip = new GzipCompression();
		gzip.setMinCompressSize(32);
		compression.putCompression(gzip);
		compression.putConfiguration("/*", CompressionConfig.builder()
				.compressIncludeMethod(HttpMethod.GET.name())
				.compressIncludeMethod(HttpMethod.POST.name())
				.compressIncludeMethod(HttpMethod.PUT.name())
				.compressExcludeMimeType(MimeTypes.OCTET_STREAM)
				// The former GzipHandler only compressed responses; keep request bodies intact.
				.decompressExcludePath("/*")
				.build());
		return compression;
	}

	@Override
	public void stop() {
		if (server != null && server.isStarted()) {
			try {
				server.stop();
			} catch (Exception e) {
				throw ExceptionUtils.unchecked(e);
			}
		}
	}

	@Override
	public ServletContextHandler getServletContextHandler() {
		return servletContextHandler;
	}
	
	@Listen
	@Transactional
	public void on(EntityPersisted event) {
		if (event.getEntity() instanceof Setting) {
			Setting setting = (Setting) event.getEntity();
			if (setting.getKey() == Setting.Key.SYSTEM) {
				SystemSetting systemSetting = (SystemSetting) setting.getValue();
				transactionService.runAfterCommit(new ClusterRunnable() {

					private static final long serialVersionUID = 1L;

					@Override
					public void run() {
						clusterService.submitToAllServers(new ClusterTask<Void>() {

							private static final long serialVersionUID = 1L;

							@Override
							public Void call() throws Exception {
								try {
									if (systemSetting.getSessionTimeout() != null) 
										servletContextHandler.getSessionHandler().setMaxInactiveInterval(systemSetting.getSessionTimeout() * 60);
									else 
										servletContextHandler.getSessionHandler().setMaxInactiveInterval(DEFAULT_SESSION_TIMEOUT);
								} catch (Throwable t) {
									logger.error("Error setting session timeout", t);
								}
								return null;
							}
							
						});
					}
					
				});
			}
		}
	}

	public Object writeReplace() throws ObjectStreamException {
		return new ManagedSerializedForm(JettyService.class);
	}

}
