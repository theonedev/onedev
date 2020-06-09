package io.onedev.server.plugin.authenticator.ldap;

import java.util.Collection;

import com.google.common.collect.Sets;

import io.onedev.commons.launcher.loader.AbstractPluginModule;
import io.onedev.commons.launcher.loader.ImplementationProvider;
import io.onedev.server.model.support.administration.authenticator.Authenticator;

/**
 * NOTE: Do not forget to rename moduleClass property defined in the pom if you've renamed this class.
 *
 */
public class LdapModule extends AbstractPluginModule {

	@Override
	protected void configure() {
		super.configure();
		
		// put your guice bindings here
		contribute(ImplementationProvider.class, new ImplementationProvider() {

			@Override
			public Class<?> getAbstractClass() {
				return Authenticator.class;
			}

			@Override
			public Collection<Class<?>> getImplementations() {
				return Sets.newHashSet(LdapAuthenticator.class, ActiveDirectoryAuthenticator.class);
			}
			
		});
	}

}
