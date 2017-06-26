package com.gitplex.server.rest;

import javax.inject.Singleton;

import org.apache.shiro.web.filter.mgt.FilterChainManager;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.servlet.ServletContainer;

import com.gitplex.launcher.loader.AbstractPluginModule;
import com.gitplex.server.rest.jersey.JerseyConfigurator;
import com.gitplex.server.rest.jersey.ResourceConfigProvider;
import com.gitplex.server.rest.jersey.DefaultServletContainer;
import com.gitplex.server.security.FilterChainConfigurator;

/**
 * NOTE: Do not forget to rename moduleClass property defined in the pom if you've renamed this class.
 *
 */
public class RestModule extends AbstractPluginModule {
	
	@Override
	protected void configure() {
		super.configure();
		
		bind(ResourceConfig.class).toProvider(ResourceConfigProvider.class).in(Singleton.class);
		bind(ServletContainer.class).to(DefaultServletContainer.class);
		
		contribute(FilterChainConfigurator.class, new FilterChainConfigurator() {

			@Override
			public void configure(FilterChainManager filterChainManager) {
				filterChainManager.createChain("/rest/**", "noSessionCreation, authcBasic");
			}
			
		});
		
		contribute(JerseyConfigurator.class, new JerseyConfigurator() {
			
			@Override
			public void configure(ResourceConfig resourceConfig) {
				resourceConfig.packages(RestModule.class.getPackage().getName());
			}
			
		});
		
	}

}
