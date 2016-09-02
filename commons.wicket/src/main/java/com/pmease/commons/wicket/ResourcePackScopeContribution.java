package com.pmease.commons.wicket;

import java.util.Collection;

import com.pmease.commons.loader.ExtensionPoint;

@ExtensionPoint
public interface ResourcePackScopeContribution {
	Collection<Class<?>> getResourcePackScopes();
}
