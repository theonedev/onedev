package com.turbodev.server.security;

import org.apache.shiro.web.filter.mgt.FilterChainManager;

import com.turbodev.launcher.loader.ExtensionPoint;

@ExtensionPoint
public interface FilterChainConfigurator {
	void configure(FilterChainManager filterChainManager);
}
