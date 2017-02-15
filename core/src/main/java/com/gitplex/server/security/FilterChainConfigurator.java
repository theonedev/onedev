package com.gitplex.server.security;

import org.apache.shiro.web.filter.mgt.FilterChainManager;

import com.gitplex.launcher.loader.ExtensionPoint;

@ExtensionPoint
public interface FilterChainConfigurator {
	void configure(FilterChainManager filterChainManager);
}
