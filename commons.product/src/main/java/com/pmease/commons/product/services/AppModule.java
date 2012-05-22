package com.pmease.commons.product.services;

import org.apache.tapestry5.SymbolConstants;
import org.apache.tapestry5.ioc.MappedConfiguration;

import com.pmease.commons.loader.PluginManager;
import com.pmease.commons.product.Plugin;

public class AppModule {

	public static void contributeFactoryDefaults(
			MappedConfiguration<String, Object> configuration, PluginManager pluginManager) {
		configuration.override(
				SymbolConstants.APPLICATION_VERSION, 
				pluginManager.getPlugin(Plugin.class).getVersion());
	}

}
