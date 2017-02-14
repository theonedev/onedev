package com.gitplex.commons.shiro;

import org.apache.shiro.web.filter.mgt.FilterChainManager;

import com.gitplex.calla.loader.ExtensionPoint;

@ExtensionPoint
public interface FilterChainConfigurator {
	void configure(FilterChainManager filterChainManager);
}
