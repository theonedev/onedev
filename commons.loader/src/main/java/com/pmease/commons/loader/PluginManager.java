package com.pmease.commons.loader;

import java.util.Collection;

public interface PluginManager {

	Collection<Plugin> getPlugins();
	
	<T extends Plugin> T getPlugin(Class<T> pluginClass);
	
	Plugin getPlugin(String pluginId);
	
	void start();
	
	void stop();
}
