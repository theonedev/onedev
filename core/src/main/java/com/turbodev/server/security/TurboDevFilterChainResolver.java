package com.turbodev.server.security;

import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.shiro.web.filter.mgt.FilterChainManager;
import org.apache.shiro.web.filter.mgt.PathMatchingFilterChainResolver;

@Singleton
public class TurboDevFilterChainResolver extends PathMatchingFilterChainResolver {

	@Inject
	public TurboDevFilterChainResolver(
			Set<FilterChainConfigurator> filterChainConfigurators, 
			BasicAuthenticationFilter basicAuthenticationFilter) {
		
		super();
		
		FilterChainManager filterChainManager = getFilterChainManager();
		
		filterChainManager.addFilter("authcBasic", basicAuthenticationFilter);
		
		for (FilterChainConfigurator configurator: filterChainConfigurators) {
			configurator.configure(filterChainManager);
		}
		
		filterChainManager.createChain("/**", "authcBasic");
	}
	
}
