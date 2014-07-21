package com.pmease.gitplex.security;

import com.pmease.commons.loader.AbstractPluginModule;
import com.pmease.commons.shiro.AbstractRealm;

/**
 * NOTE: Do not forget to rename moduleClass property defined in the pom if you've renamed this class.
 *
 */
public class SecurityModule extends AbstractPluginModule {

	@Override
	protected void configure() {
		super.configure();

		bind(AbstractRealm.class).to(SecurityRealm.class);
	}

}
