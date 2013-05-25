package com.pmease.commons.product;

import java.io.File;
import java.util.Collection;
import java.util.Properties;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.bio.SocketConnector;
import org.eclipse.jetty.server.ssl.SslSocketConnector;
import org.eclipse.jetty.servlet.ErrorPageErrorHandler;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableList;
import com.pmease.commons.bootstrap.Bootstrap;
import com.pmease.commons.hibernate.AbstractEntity;
import com.pmease.commons.hibernate.extensionpoints.ModelContribution;
import com.pmease.commons.jetty.ClasspathAssetServlet;
import com.pmease.commons.jetty.FileAssetServlet;
import com.pmease.commons.jetty.extensionpoints.ServerConfigurator;
import com.pmease.commons.jetty.extensionpoints.ServletContextConfigurator;
import com.pmease.commons.loader.AbstractPlugin;
import com.pmease.commons.product.model.User;
import com.pmease.commons.product.web.asset.AssetLocator;
import com.pmease.commons.util.ClassUtils;
import com.pmease.commons.util.FileUtils;

public class Plugin extends AbstractPlugin {

	private static final Logger logger = LoggerFactory.getLogger(Plugin.class);
	
	private final Properties serverProps;
	
	public Plugin() {
		serverProps = FileUtils.loadProperties(new File(Bootstrap.getConfDir(), "server.properties"));
	}
	
	@Override
	public Collection<?> getExtensions() {
		return ImmutableList.of(				
			new ServerConfigurator() {
		
				@SuppressWarnings("deprecation")
				@Override
				public void configure(Server server) {
					SocketConnector connector = new SocketConnector();
					String httpPort = serverProps.getProperty("httpPort");
					if (StringUtils.isNotBlank(httpPort)) {
						connector.setPort(Integer.parseInt(httpPort));
						server.addConnector(connector);
					}
					
					String httpsPort = serverProps.getProperty("httpsPort");
					if (StringUtils.isNotBlank(httpsPort)) {
						SslSocketConnector sslConnector = new SslSocketConnector();
						sslConnector.setPort(Integer.parseInt(httpsPort));
						
						String keystorePath = serverProps.getProperty("sslKeystorePath");
						if (StringUtils.isBlank(keystorePath))
							keystorePath = "sample.keystore";
						String keystorePassword = serverProps.getProperty("sslKeystorePassword");
						if (StringUtils.isBlank(keystorePassword))
							keystorePassword = "123456";
						String keystoreKeyPassword = serverProps.getProperty("sslKeystoreKeyPassword");
						if (StringUtils.isBlank(keystoreKeyPassword))
							keystoreKeyPassword = "123456";
						
						File keystoreFile = new File(keystorePath);
						if (!keystoreFile.isAbsolute())
							keystoreFile = new File(Bootstrap.getConfDir(), keystorePath);
						
						sslConnector.setKeystore(keystoreFile.getAbsolutePath());
						sslConnector.setPassword(keystorePassword);
						sslConnector.setKeyPassword(keystoreKeyPassword);
						
						server.addConnector(sslConnector);
					}
					
					if (StringUtils.isBlank(httpPort) && StringUtils.isBlank(httpsPort))
						throw new RuntimeException("Either httpPort or httpsPort or both should be enabled.");
				}
			}, 
			new ServletContextConfigurator() {

				@Override
				public void configure(ServletContextHandler context) {
					/*
					 * Configure a servlet to serve contents under site folder. Site folder can be used 
					 * to hold site specific web assets.   
					 */
					File siteDir = new File(Bootstrap.installDir, "site");
					ServletHolder servletHolder = new ServletHolder(new FileAssetServlet(siteDir));
					context.addServlet(servletHolder, "/site/*");
					context.addServlet(servletHolder, "/robots.txt");
					
					servletHolder = new ServletHolder(new ClasspathAssetServlet(AssetLocator.class));
					context.addServlet(servletHolder, "/asset/*");
					context.addServlet(servletHolder, "/favicon.ico");
					
					ErrorPageErrorHandler errorHandler = (ErrorPageErrorHandler) context.getErrorHandler();
					errorHandler.addErrorPage(HttpServletResponse.SC_NOT_FOUND, "/asset/404.html");
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
		logger.info("Product has been started successfully.");
	}

}
