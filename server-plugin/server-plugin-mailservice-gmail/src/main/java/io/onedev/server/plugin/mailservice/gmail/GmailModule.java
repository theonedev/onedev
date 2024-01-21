package io.onedev.server.plugin.mailservice.gmail;

import com.google.common.collect.Sets;
import io.onedev.commons.loader.AbstractPluginModule;
import io.onedev.commons.loader.ImplementationProvider;
import io.onedev.server.model.support.administration.mailservice.MailService;

import java.util.Collection;

/**
 * NOTE: Do not forget to rename moduleClass property defined in the pom if you've renamed this class.
 *
 */
public class GmailModule extends AbstractPluginModule {

	@Override
	protected void configure() {
		super.configure();
		
		// put your guice bindings here
		contribute(ImplementationProvider.class, new ImplementationProvider() {

			@Override
			public Class<?> getAbstractClass() {
				return MailService.class;
			}

			@Override
			public Collection<Class<?>> getImplementations() {
				return Sets.newHashSet(GmailMailService.class);
			}
			
		});
	}

}
