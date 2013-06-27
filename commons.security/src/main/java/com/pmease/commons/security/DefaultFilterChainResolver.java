package com.pmease.commons.security;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.shiro.web.filter.mgt.FilterChainManager;
import org.apache.shiro.web.filter.mgt.PathMatchingFilterChainResolver;

import com.pmease.commons.loader.PluginManager;
import com.pmease.commons.security.extensionpoint.FilterChainConfigurator;

@Singleton
public class DefaultFilterChainResolver extends PathMatchingFilterChainResolver {

	@Inject
	public DefaultFilterChainResolver(
			PluginManager pluginManager, 
			BasicAuthenticationFilter basicAuthenticationFilter) {
		
		super();
		
		FilterChainManager filterChainManager = getFilterChainManager();
		
		filterChainManager.addFilter("authcBasic", basicAuthenticationFilter);
		
		for (FilterChainConfigurator configurator: pluginManager.getExtensions(FilterChainConfigurator.class)) {
			configurator.configure(filterChainManager);
		}
		
		filterChainManager.createChain("/**", "authcBasic");
	}
	
}
