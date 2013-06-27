package com.pmease.commons.security.extensionpoint;

import org.apache.shiro.web.filter.mgt.FilterChainManager;

public interface FilterChainConfigurator {
	void configure(FilterChainManager filterChainManager);
}
