package com.pmease.commons.jersey;

import org.apache.shiro.web.filter.mgt.FilterChainManager;

import com.pmease.commons.loader.AbstractPluginModule;
import com.pmease.commons.shiro.FilterChainConfigurator;
import com.pmease.commons.util.EasyMap;
import com.sun.jersey.guice.JerseyServletModule;
import com.sun.jersey.guice.spi.container.servlet.GuiceContainer;

/**
 * NOTE: Do not forget to rename moduleClass property defined in the pom if
 * you've renamed this class.
 * 
 */
public class JerseyModule extends AbstractPluginModule {

	public static final String REST_PATH = "rest";

	@Override
	protected void configure() {
		super.configure();

		install(new JerseyServletModule() {

			protected void configureServlets() {
				// Bind at least one resource here as otherwise Jersey will report error.
				bind(DummyResource.class);

				// Route all RESTful requests through GuiceContainer
				serve(getRestPath()).with(
						GuiceContainer.class, 
						EasyMap.of("com.sun.jersey.api.json.POJOMappingFeature", "true"));
			}
			
			protected String getRestPath() {
				return "/" + REST_PATH + "/*";
			}
			
		});
		
		contribute(FilterChainConfigurator.class, new FilterChainConfigurator() {

			@Override
			public void configure(FilterChainManager filterChainManager) {
				filterChainManager.createChain("/" + REST_PATH + "/**", "noSessionCreation, authcBasic");
			}
			
		});
	}

}
