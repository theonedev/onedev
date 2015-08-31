package com.pmease.commons.loader;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.pmease.commons.util.dependency.DependencyHelper;

@Singleton
public class DefaultPluginManager implements PluginManager {
	
	// use linked hash map here to keep plugins in order
	private final Map<String, Plugin> pluginMap = new LinkedHashMap<String, Plugin>();

	private final ExecutorService executorService;
	
	/**
	 * Construct plugin map in dependency order. Plugins without dependencies comes first in the 
	 * linked hash map.
	 *    
	 * @param plugins
	 */
	@Inject
	public DefaultPluginManager(final Set<Plugin> plugins, final ExecutorService executorService) {
		for (Plugin plugin: plugins)
			pluginMap.put(plugin.getId(), plugin);
		
		for (Plugin plugin: DependencyHelper.sortDependencies(pluginMap)) {
			// make sure the plugin map is in sorted order.
			pluginMap.remove(plugin.getId());
			pluginMap.put(plugin.getId(), plugin);
		}
		
		this.executorService = executorService;
	}

	@Override
	public void start() {
		for (Plugin plugin: pluginMap.values())
			plugin.start();
		List<Plugin> reversed = new ArrayList<Plugin>(pluginMap.values());
		Collections.reverse(reversed);
		for (Plugin plugin: reversed)
			plugin.postStart();
	}

	@Override
	public void stop() {
		executorService.shutdown();
		
		for (Plugin plugin: pluginMap.values())
			plugin.preStop();
		List<Plugin> reversed = new ArrayList<Plugin>(pluginMap.values());
		Collections.reverse(reversed);
		for (Plugin plugin: reversed)
			plugin.stop();
	}

	@Override
	public Collection<Plugin> getPlugins() {
		return Collections.unmodifiableCollection(pluginMap.values());
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T extends Plugin> T getPlugin(Class<T> pluginClass) {
		for (Plugin plugin: pluginMap.values()) {
			if (plugin.getClass() == pluginClass)
				return (T) plugin;
		}
		throw new RuntimeException("Unable to find plugin with class '" + pluginClass + "'.");
	}
	
	@Override
	public Plugin getPlugin(String pluginId) {
		if (pluginMap.containsKey(pluginId))
			return pluginMap.get(pluginId);
		else
			throw new RuntimeException("Unable to find plugin with id '" + pluginId + "'.");
	}

}
