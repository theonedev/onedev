package io.onedev.server.jetty;

import java.io.ObjectStreamException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import javax.servlet.DispatcherType;
import javax.servlet.http.HttpServletResponse;

import org.apache.tika.mime.MimeTypes;
import org.eclipse.jetty.http.HttpCookie.SameSite;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.gzip.GzipHandler;
import org.eclipse.jetty.server.session.SessionDataStoreFactory;
import org.eclipse.jetty.servlet.ErrorPageErrorHandler;
import org.eclipse.jetty.servlet.ServletContextHandler;

import com.google.inject.servlet.GuiceFilter;

import io.onedev.commons.bootstrap.Bootstrap;
import io.onedev.commons.loader.ManagedSerializedForm;
import io.onedev.commons.utils.ExceptionUtils;
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
public class DefaultJettyService implements JettyService, Provider<ServletContextHandler>, Serializable {

	private static final int MAX_CONTENT_SIZE = 5000000;

	@Inject
	private SessionDataStoreFactory sessionDataStoreFactory;
	
	private Server server;
	
	private ServletContextHandler servletContextHandler;

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
	public ServletContextHandler get() {
		return servletContextHandler;
	}

	@Override
	public void start() {
		server = new Server();

		server.addBean(sessionDataStoreFactory);
		
        servletContextHandler = new ServletContextHandler(ServletContextHandler.SESSIONS);
        servletContextHandler.setMaxFormContentSize(MAX_CONTENT_SIZE);

        servletContextHandler.setClassLoader(DefaultJettyService.class.getClassLoader());
        
        servletContextHandler.setErrorHandler(new ErrorPageErrorHandler());
        servletContextHandler.addFilter(DisableTraceFilter.class, "/*", EnumSet.of(DispatcherType.REQUEST));
        
        servletContextHandler.getSessionHandler().setSessionIdPathParameterName(null);
        servletContextHandler.getSessionHandler().setSameSite(SameSite.LAX);  
        servletContextHandler.getSessionHandler().setHttpOnly(true);
		if (settingService.getSystemSetting() != null) 
			servletContextHandler.getSessionHandler().setMaxInactiveInterval(settingService.getSystemSetting().getSessionTimeout() * 60);

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
	    errorHandler.addErrorPage(HttpServletResponse.SC_NOT_FOUND, "/~errors/404");
	    servletContextHandler.setErrorHandler(errorHandler);

        GzipHandler gzipHandler = new GzipHandler();
		gzipHandler.setHandler(servletContextHandler);
		gzipHandler.setIncludedMethods(HttpMethod.GET.name(), HttpMethod.POST.name(), HttpMethod.PUT.name());
		gzipHandler.setExcludedMimeTypes(MimeTypes.OCTET_STREAM);

        server.setHandler(gzipHandler);
        
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
								servletContextHandler.getSessionHandler().setMaxInactiveInterval(systemSetting.getSessionTimeout() * 60);
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
