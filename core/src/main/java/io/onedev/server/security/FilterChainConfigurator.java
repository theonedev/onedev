package io.onedev.server.security;

import org.apache.shiro.web.filter.mgt.FilterChainManager;

import io.onedev.commons.launcher.loader.ExtensionPoint;

@ExtensionPoint
public interface FilterChainConfigurator {
	void configure(FilterChainManager filterChainManager);
}
