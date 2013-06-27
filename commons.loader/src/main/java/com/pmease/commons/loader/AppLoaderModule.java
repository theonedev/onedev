package com.pmease.commons.loader;

import com.google.inject.AbstractModule;

public class AppLoaderModule extends AbstractModule {

	@Override
	protected void configure() {
		bindConstant().annotatedWith(AppName.class).to("Application");
		bind(PluginManager.class).to(DefaultPluginManager.class);
	}

}
