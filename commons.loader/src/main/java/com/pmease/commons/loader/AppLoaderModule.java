package com.pmease.commons.loader;

import com.google.inject.AbstractModule;

public class AppLoaderModule extends AbstractModule {

	@Override
	protected void configure() {
		bind(PluginManager.class).to(PluginManagerImpl.class);
	}

}
