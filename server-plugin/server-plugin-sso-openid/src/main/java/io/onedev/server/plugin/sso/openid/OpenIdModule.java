package io.onedev.server.plugin.sso.openid;

import java.util.Collection;

import com.google.common.collect.Sets;

import io.onedev.commons.launcher.loader.AbstractPluginModule;
import io.onedev.commons.launcher.loader.ImplementationProvider;
import io.onedev.server.model.support.administration.sso.SsoConnector;

/**
 * NOTE: Do not forget to rename moduleClass property defined in the pom if you've renamed this class.
 *
 */
public class OpenIdModule extends AbstractPluginModule {

	@Override
	protected void configure() {
		super.configure();
		
		// put your guice bindings here
		contribute(ImplementationProvider.class, new ImplementationProvider() {

			@Override
			public Class<?> getAbstractClass() {
				return SsoConnector.class;
			}

			@Override
			public Collection<Class<?>> getImplementations() {
				return Sets.newHashSet(GitHubConnector.class, OpenIdConnector.class);
			}
			
		});
	}

}
