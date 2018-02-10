package com.turbodev.server.web;

import java.util.Collection;

import com.turbodev.launcher.loader.ExtensionPoint;

@ExtensionPoint
public interface ResourcePackScopeContribution {
	Collection<Class<?>> getResourcePackScopes();
}
