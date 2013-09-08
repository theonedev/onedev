package com.pmease.gitop.core;

import org.hibernate.cfg.NamingStrategy;

import com.pmease.commons.loader.AbstractPlugin;
import com.pmease.commons.loader.AbstractPluginModule;
import com.pmease.commons.persistence.PrefixedNamingStrategy;
import com.pmease.commons.security.AbstractRealm;
import com.pmease.commons.web.AbstractWicketConfig;
import com.pmease.gitop.core.permission.UserRealm;

/**
 * NOTE: Do not forget to rename moduleClass property defined in the pom if you've renamed this class.
 *
 */
public class CoreModule extends AbstractPluginModule {

	@Override
	protected void configure() {
		super.configure();
		
		bind(AbstractWicketConfig.class).to(WicketConfig.class);		
		bind(AbstractRealm.class).to(UserRealm.class);
		
		bind(NamingStrategy.class).toInstance(new PrefixedNamingStrategy("G"));
		
		bind(Gitop.class);
	}

	@Override
	protected Class<? extends AbstractPlugin> getPluginClass() {
		return CorePlugin.class;
	}

}
