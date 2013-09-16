package com.pmease.commons.shiro;

import org.apache.shiro.web.filter.mgt.FilterChainManager;

import com.pmease.commons.loader.ExtensionPoint;

@ExtensionPoint
public interface FilterChainConfigurator {
	void configure(FilterChainManager filterChainManager);
}
