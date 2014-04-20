package com.pmease.gitop.web;

import com.pmease.commons.editable.EditSupport;
import com.pmease.commons.jersey.JerseyConfigurator;
import com.pmease.commons.jersey.JerseyEnvironment;
import com.pmease.commons.jetty.ServletConfigurator;
import com.pmease.commons.loader.AbstractPluginModule;
import com.pmease.commons.wicket.AbstractWicketConfig;
import com.pmease.gitop.model.validation.UserNameReservation;
import com.pmease.gitop.web.editable.EditSupportLocator;
import com.pmease.gitop.web.page.repository.source.blob.renderer.BlobRendererFactory;
import com.pmease.gitop.web.resource.ResourceLocator;
import com.pmease.gitop.web.service.ServiceModule;

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
		contribute(ServletConfigurator.class, WebServletConfigurator.class);
		contribute(UserNameReservation.class, WebUserNameReservation.class);
		
		contribute(JerseyConfigurator.class, new JerseyConfigurator() {
			
			@Override
			public void configure(JerseyEnvironment environment) {
				environment.addComponentFromPackage(ResourceLocator.class);
			}
			
		});
		
		contributeFromPackage(EditSupport.class, EditSupportLocator.class);

		install(new ServiceModule());
		
		bind(BlobRendererFactory.class);
	}

}
