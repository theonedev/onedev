package com.pmease.commons.product;

import java.io.File;
import java.util.Collection;
import java.util.EnumSet;
import java.util.Properties;

import javax.inject.Inject;
import javax.servlet.DispatcherType;

import org.apache.tapestry5.TapestryFilter;
import org.apache.tapestry5.internal.InternalConstants;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.bio.SocketConnector;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableList;
import com.pmease.commons.bootstrap.Bootstrap;
import com.pmease.commons.hibernate.AbstractEntity;
import com.pmease.commons.hibernate.HibernateFilter;
import com.pmease.commons.hibernate.extensionpoints.ModelContribution;
import com.pmease.commons.jetty.JettyUtils;
import com.pmease.commons.jetty.extensionpoints.ServerConfigurator;
import com.pmease.commons.jetty.extensionpoints.ServletContextConfigurator;
import com.pmease.commons.loader.AbstractPlugin;
import com.pmease.commons.product.model.User;
import com.pmease.commons.util.ClassUtils;
import com.pmease.commons.util.FileUtils;

public class Plugin extends AbstractPlugin {

	private static final Logger logger = LoggerFactory.getLogger(Plugin.class);
	
	private Properties serverProps;
	
	private final HibernateFilter hibernateFilter;
	
	private final TapestryFilter tapestryFilter;
	
	@Inject
	public Plugin(HibernateFilter hibernateFilter, TapestryFilter tapestryFilter) {
		this.hibernateFilter = hibernateFilter;
		this.tapestryFilter = tapestryFilter;
		serverProps = FileUtils.loadProperties(new File(Bootstrap.getConfDir(), "server.properties"));
	}
	
	@Override
	public Collection<?> getExtensions() {
		return ImmutableList.of(				
			new ServerConfigurator() {
		
				@Override
				public void configure(Server server) {
					SocketConnector connector = new SocketConnector();
					connector.setPort(Integer.parseInt(serverProps.getProperty("httpPort")));
					server.addConnector(connector);
				}
			}, 
			new ServletContextConfigurator() {

				@Override
				public void configure(ServletContextHandler context) {
			        context.getSessionHandler().getSessionManager()
			        		.setMaxInactiveInterval(Integer.parseInt(serverProps.getProperty("sessionTimeout")));

			        File resourceDir = new File(Bootstrap.installDir, "resource");
			        context.setResourceBase(resourceDir.getAbsolutePath());
			        
			        ServletHolder servletHolder = JettyUtils.createResourceServletHolder();
			        for (String path: resourceDir.list()) 
			        	context.addServlet(servletHolder, "/" + path);
			        
					context.addServlet(JettyUtils.createResourceServletHolder(), "/");

					FilterHolder filterHolder = new FilterHolder(hibernateFilter);
					context.addFilter(filterHolder, "/*", EnumSet.of(DispatcherType.REQUEST)); 
					
					filterHolder = new FilterHolder(tapestryFilter);
					filterHolder.setName("app");
					context.setInitParameter(InternalConstants.TAPESTRY_APP_PACKAGE_PARAM, Plugin.class.getPackage().getName());
					context.addFilter(filterHolder, "/*", EnumSet.of(DispatcherType.REQUEST)); 
					
					context.addServlet(GitServlet.class, "*.git");
				}
				
			}, 
			new ModelContribution() {

				@Override
				public Collection<Class<AbstractEntity>> getModelClasses() {
					return ClassUtils.findSubClasses(AbstractEntity.class, User.class);
				}
				
			}
		);
	}

	@Override
	public void postStartDependents() {
		logger.info("Commons.product has been started successfully.");
	}

}
