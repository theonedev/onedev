package ${package};

import java.io.File;
import java.util.Collection;
import java.util.Properties;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.bio.SocketConnector;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.pmease.commons.bootstrap.Bootstrap;
import com.pmease.commons.hibernate.AbstractEntity;
import com.pmease.commons.hibernate.extensionpoints.ModelContribution;
import com.pmease.commons.jetty.JettyUtils;
import com.pmease.commons.jetty.extensionpoints.ServerConfigurator;
import com.pmease.commons.jetty.extensionpoints.ServletContextConfigurator;
import com.pmease.commons.loader.AbstractPlugin;
import com.pmease.commons.util.ClassUtils;
import com.pmease.commons.util.FileUtils;
import ${package}.model.User;

public class Plugin extends AbstractPlugin {

	private static final Logger logger = LoggerFactory.getLogger(Plugin.class);
	
	private Properties serverProps;
	
	public Plugin() {
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
					context.setResourceBase(new File(Bootstrap.installDir, "resource").getAbsolutePath());
					
					ServletHolder servletHolder = JettyUtils.getDefaultServletHolder(context);
					Preconditions.checkNotNull(servletHolder);
					context.addServlet(servletHolder, "/images/*");
					context.addServlet(servletHolder, "/scripts/*");
					context.addServlet(servletHolder, "/styles/*");
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
		logger.info("${artifactId.toUpperCase().charAt(0)}${artifactId.substring(1)} has been started successfully.");
	}

}
