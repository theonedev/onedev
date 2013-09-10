package com.pmease.commons.jersey;

import com.pmease.commons.loader.AbstractPlugin;
import com.pmease.commons.loader.AbstractPluginModule;
import com.pmease.commons.shiro.extensionpoint.FilterChainConfigurator;
import com.pmease.commons.util.EasyMap;
import com.sun.jersey.guice.JerseyServletModule;
import com.sun.jersey.guice.spi.container.servlet.GuiceContainer;

/**
 * NOTE: Do not forget to rename moduleClass property defined in the pom if
 * you've renamed this class.
 * 
 */
public class JerseyModule extends AbstractPluginModule {

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
				return "/" + JerseyPlugin.REST_PATH + "/*";
			}
			
		});
		
		addExtension(FilterChainConfigurator.class, JerseyFilterChainConfigurator.class);
	}

	@Override
	protected Class<? extends AbstractPlugin> getPluginClass() {
		return JerseyPlugin.class;
	}

}
