package com.pmease.commons.git;

import com.pmease.commons.loader.AbstractPlugin;
import com.pmease.commons.loader.AbstractPluginModule;

/**
 * NOTE: Do not forget to rename moduleClass property defined in the pom if you've renamed this class.
 *
 */
public class GitModule extends AbstractPluginModule {

	@Override
	protected void configure() {
		super.configure();
		
		// put your guice bindings here
	}

	@Override
	protected Class<? extends AbstractPlugin> getPluginClass() {
		return GitPlugin.class;
	}

}
