package com.pmease.gitop.core;

import com.pmease.commons.loader.AbstractPlugin;
import com.pmease.commons.loader.AbstractPluginModule;
import com.pmease.commons.loader.AppName;
import com.pmease.commons.security.AbstractRealm;
import com.pmease.commons.web.AbstractWicketConfig;

/**
 * NOTE: Do not forget to rename moduleClass property defined in the pom if you've renamed this class.
 *
 */
public class CoreModule extends AbstractPluginModule {

	public static final String PRODUCT_NAME = "Gitop";
	
	@Override
	protected void configure() {
		super.configure();
		
		bind(AbstractWicketConfig.class).to(WicketConfig.class);		
		bind(AbstractRealm.class).to(UserRealm.class);
		
		bindConstant().annotatedWith(AppName.class).to(PRODUCT_NAME);
		
		bind(Gitop.class);
	}

	@Override
	protected Class<? extends AbstractPlugin> getPluginClass() {
		return CorePlugin.class;
	}

}
