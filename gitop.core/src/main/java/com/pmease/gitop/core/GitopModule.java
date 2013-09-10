package com.pmease.gitop.core;

import org.hibernate.cfg.NamingStrategy;

import com.pmease.commons.hibernate.ModelProvider;
import com.pmease.commons.hibernate.PrefixedNamingStrategy;
import com.pmease.commons.loader.AbstractPlugin;
import com.pmease.commons.loader.AbstractPluginModule;
import com.pmease.commons.shiro.AbstractRealm;
import com.pmease.gitop.core.permission.UserRealm;

/**
 * NOTE: Do not forget to rename moduleClass property defined in the pom if you've renamed this class.
 *
 */
public class GitopModule extends AbstractPluginModule {

	@Override
	protected void configure() {
		super.configure();
		
		bind(AbstractRealm.class).to(UserRealm.class);
		
		addExtension(ModelProvider.class, CoreModelProvider.class);
		bind(NamingStrategy.class).toInstance(new PrefixedNamingStrategy("G"));
		
		bind(Gitop.class);
	}

	@Override
	protected Class<? extends AbstractPlugin> getPluginClass() {
		return Gitop.class;
	}

}
