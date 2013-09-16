package com.pmease.commons.loader;

import java.util.Collection;

import com.google.inject.ImplementedBy;
import com.pmease.commons.bootstrap.Lifecycle;

@ImplementedBy(DefaultPluginManager.class)
public interface PluginManager extends Lifecycle {

	Collection<Plugin> getPlugins();
	
	<T extends Plugin> T getPlugin(Class<T> pluginClass);
	
	Plugin getPlugin(String pluginId);
}
