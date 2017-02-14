package com.gitplex.commons.wicket;

import java.util.Collection;

import com.gitplex.calla.loader.ExtensionPoint;

@ExtensionPoint
public interface ResourcePackScopeContribution {
	Collection<Class<?>> getResourcePackScopes();
}
