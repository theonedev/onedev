package com.pmease.gitplex.web;

import org.apache.tika.mime.MimeTypes;
import org.glassfish.jersey.server.ResourceConfig;

import com.pmease.commons.jersey.JerseyConfigurator;
import com.pmease.commons.jetty.ServletConfigurator;
import com.pmease.commons.loader.AbstractPluginModule;
import com.pmease.commons.wicket.AbstractWicketConfig;
import com.pmease.commons.wicket.editable.EditSupport;
import com.pmease.gitplex.core.validation.UserNameReservation;
import com.pmease.gitplex.web.editable.EditSupportLocator;
import com.pmease.gitplex.web.page.repository.info.code.blob.renderer.BlobRendererFactory;
import com.pmease.gitplex.web.resource.ResourceLocator;

/**
 * NOTE: Do not forget to rename moduleClass property defined in the pom if you've renamed this class.
 *
 */
public class WebModule extends AbstractPluginModule {

	@Override
	protected void configure() {
		super.configure();
		
		// put your guice bindings here
		bind(AbstractWicketConfig.class).to(WicketConfig.class);		
		contribute(ServletConfigurator.class, WebServletConfigurator.class);
		contribute(UserNameReservation.class, WebUserNameReservation.class);
		
		contribute(JerseyConfigurator.class, new JerseyConfigurator() {
			
			@Override
			public void configure(ResourceConfig resourceConfig) {
				resourceConfig.packages(true, ResourceLocator.class.getPackage().getName());
			}
			
		});
		
		contributeFromPackage(EditSupport.class, EditSupportLocator.class);

		bind(MimeTypes.class).toInstance(MimeTypes.getDefaultMimeTypes());
		
		bind(BlobRendererFactory.class);
	}

}
