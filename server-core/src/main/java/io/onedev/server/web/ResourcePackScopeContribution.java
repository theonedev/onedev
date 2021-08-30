package io.onedev.server.web;

import java.util.Collection;

import io.onedev.commons.loader.ExtensionPoint;

@ExtensionPoint
public interface ResourcePackScopeContribution {
	Collection<Class<?>> getResourcePackScopes();
}
