package com.pmease.gitop.rest;

import org.apache.shiro.web.filter.mgt.FilterChainManager;

import com.pmease.commons.jersey.JerseyConfigurator;
import com.pmease.commons.jersey.JerseyEnvironment;
import com.pmease.commons.jetty.ServletConfigurator;
import com.pmease.commons.loader.AbstractPluginModule;
import com.pmease.commons.shiro.FilterChainConfigurator;
import com.pmease.gitop.rest.resource.ResourceLocator;

/**
 * NOTE: Do not forget to rename moduleClass property defined in the pom if you've renamed this class.
 *
 */
public class RestModule extends AbstractPluginModule {

	public static final String SERVLET_PATH = "/rest";
	
	@Override
	protected void configure() {
		super.configure();
		
		contribute(ServletConfigurator.class, RestServletConfigurator.class);
		contribute(JerseyConfigurator.class, new JerseyConfigurator() {
			
			@Override
			public void configure(JerseyEnvironment environment) {
				environment.addComponentFromPackage(ResourceLocator.class);
			}
			
		});
		
		contribute(FilterChainConfigurator.class, new FilterChainConfigurator() {

			@Override
			public void configure(FilterChainManager filterChainManager) {
				filterChainManager.createChain(SERVLET_PATH + "/**", "noSessionCreation, authcBasic");
			}
			
		});
		
	}

}
