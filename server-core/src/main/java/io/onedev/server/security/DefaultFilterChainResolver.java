package io.onedev.server.security;

import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.shiro.web.filter.mgt.FilterChainManager;
import org.apache.shiro.web.filter.mgt.PathMatchingFilterChainResolver;

@Singleton
public class DefaultFilterChainResolver extends PathMatchingFilterChainResolver {

	@Inject
	public DefaultFilterChainResolver(
			Set<FilterChainConfigurator> filterChainConfigurators, 
			BasicAuthenticationFilter basicAuthenticationFilter, 
			BearerAuthenticationFilter bearerAuthenticationFilter) {
		
		super();
		
		FilterChainManager filterChainManager = getFilterChainManager();
		
		filterChainManager.addFilter("authcBasic", basicAuthenticationFilter);
		filterChainManager.addFilter("authcBearer", bearerAuthenticationFilter);
		
		for (FilterChainConfigurator configurator: filterChainConfigurators)
			configurator.configure(filterChainManager);
		
		filterChainManager.createChain("/**", "authcBasic, authcBearer");
	}
	
}
