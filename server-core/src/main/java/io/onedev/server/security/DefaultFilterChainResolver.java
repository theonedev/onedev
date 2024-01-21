package io.onedev.server.security;

import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.onedev.server.pack.PackFilter;
import org.apache.shiro.web.filter.mgt.FilterChainManager;
import org.apache.shiro.web.filter.mgt.PathMatchingFilterChainResolver;

@Singleton
public class DefaultFilterChainResolver extends PathMatchingFilterChainResolver {

	@Inject
	public DefaultFilterChainResolver(
			Set<FilterChainConfigurator> filterChainConfigurators,
			BasicAuthenticationFilter basicAuthenticationFilter,
			BearerAuthenticationFilter bearerAuthenticationFilter,
			PackFilter packFilter) {
		
		super();
		
		FilterChainManager filterChainManager = getFilterChainManager();
		
		filterChainManager.addFilter("authcBasic", basicAuthenticationFilter);
		filterChainManager.addFilter("authcBearer", bearerAuthenticationFilter);
		filterChainManager.addFilter("pack", packFilter);
		
		for (FilterChainConfigurator configurator: filterChainConfigurators)
			configurator.configure(filterChainManager);
		
		filterChainManager.createChain("/**", "pack, authcBasic, authcBearer");
	}
	
}
