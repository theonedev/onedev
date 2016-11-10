package com.gitplex.commons.loader;

import java.util.Collection;

public interface PluginManager {

	Collection<Plugin> getPlugins();
	
	<T extends Plugin> T getPlugin(Class<T> pluginClass);
	
	Plugin getPlugin(String pluginId);
	
	Plugin getProduct();
	
	void start();
	
	void stop();
}
