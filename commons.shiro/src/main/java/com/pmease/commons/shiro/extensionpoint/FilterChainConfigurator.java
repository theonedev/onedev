package com.pmease.commons.shiro.extensionpoint;

import org.apache.shiro.web.filter.mgt.FilterChainManager;

public interface FilterChainConfigurator {
	void configure(FilterChainManager filterChainManager);
}
