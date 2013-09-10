package com.pmease.commons.jersey;

import org.apache.shiro.web.filter.mgt.FilterChainManager;

import com.pmease.commons.shiro.extensionpoint.FilterChainConfigurator;

public class JerseyFilterChainConfigurator implements FilterChainConfigurator {

	@Override
	public void configure(FilterChainManager filterChainManager) {
		filterChainManager.createChain("/" + JerseyPlugin.REST_PATH + "/**", "noSessionCreation, authcBasic");
	}

}
