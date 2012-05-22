package com.pmease.commons.loader;

import java.util.Collection;

import com.pmease.commons.bootstrap.Lifecycle;

public interface PluginManager extends Lifecycle {

	<T> Collection<T> getExtensions(Class<T> extensionPoint);
	
	Collection<AbstractPlugin> getPlugins();
	
	<T extends AbstractPlugin> T getPlugin(Class<T> pluginClass);
	
	AbstractPlugin getPlugin(String pluginId);
}
