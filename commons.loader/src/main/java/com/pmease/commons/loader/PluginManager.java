package com.pmease.commons.loader;

import java.util.Collection;
import java.util.Map;

import com.pmease.commons.bootstrap.Lifecycle;

public interface PluginManager extends Lifecycle {

	<T> Collection<T> getExtensions(Class<T> extensionPoint);
	
	Map<String, AbstractPlugin> getPluginMap();
}
