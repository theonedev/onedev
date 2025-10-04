package io.onedev.server.plugin.mail.office365;

import com.google.common.collect.Sets;
import io.onedev.commons.loader.AbstractPluginModule;
import io.onedev.commons.loader.ImplementationProvider;
import io.onedev.server.model.support.administration.mailservice.MailConnector;

import java.util.Collection;

/**
 * NOTE: Do not forget to rename moduleClass property defined in the pom if you've renamed this class.
 *
 */
public class Office365Module extends AbstractPluginModule {

	@Override
	protected void configure() {
		super.configure();
		
		// put your guice bindings here
		contribute(ImplementationProvider.class, new ImplementationProvider() {

			@Override
			public Class<?> getAbstractClass() {
				return MailConnector.class;
			}

			@Override
			public Collection<Class<?>> getImplementations() {
				return Sets.newHashSet(Office365Connector.class);
			}
			
		});
	}

}
