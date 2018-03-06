package io.onedev.server.web;

import java.util.Collection;

import io.onedev.launcher.loader.ExtensionPoint;

@ExtensionPoint
public interface ResourcePackScopeContribution {
	Collection<Class<?>> getResourcePackScopes();
}
