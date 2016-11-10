package com.gitplex.commons.wicket;

import java.util.Collection;

import com.gitplex.commons.loader.ExtensionPoint;

@ExtensionPoint
public interface ResourcePackScopeContribution {
	Collection<Class<?>> getResourcePackScopes();
}
