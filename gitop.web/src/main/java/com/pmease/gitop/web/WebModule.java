package com.pmease.gitop.web;

import javax.inject.Singleton;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.servlet.ErrorPageErrorHandler;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

import com.codahale.dropwizard.jackson.Jackson;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Provides;
import com.pmease.commons.jetty.ClasspathAssetServlet;
import com.pmease.commons.jetty.ServletContextConfigurator;
import com.pmease.commons.loader.AbstractPluginModule;
import com.pmease.commons.wicket.AbstractWicketConfig;
import com.pmease.gitop.web.assets.AssetLocator;
import com.pmease.gitop.web.resource.RestResourceModule;

/**
 * NOTE: Do not forget to rename moduleClass property defined in the pom if you've renamed this class.
 *
 */
public class WebModule extends AbstractPluginModule {

	@Override
	protected void configure() {
		super.configure();
		
		// put your guice bindings here
		bind(AbstractWicketConfig.class).to(GitopWebApp.class);		
		bind(SitePaths.class).in(Singleton.class);
		
		contribute(ServletContextConfigurator.class, new ServletContextConfigurator() {
			
			@Override
			public void configure(ServletContextHandler context) {
				ServletHolder servletHolder = new ServletHolder(new ClasspathAssetServlet(AssetLocator.class));
				context.addServlet(servletHolder, "/assets/*");
				context.addServlet(servletHolder, "/favicon.ico");
				
				ErrorPageErrorHandler errorHandler = (ErrorPageErrorHandler) context.getErrorHandler();
				errorHandler.addErrorPage(HttpServletResponse.SC_NOT_FOUND, "/assets/404.html");
			}
			
		});

		install(new RestResourceModule());
	}

	@Provides
	@Singleton
	public ObjectMapper objectMapper() {
		return Jackson.newObjectMapper();
	}
}
