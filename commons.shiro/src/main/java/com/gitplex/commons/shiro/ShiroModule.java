package com.gitplex.commons.shiro;

import javax.inject.Singleton;

import org.apache.shiro.authc.credential.DefaultPasswordService;
import org.apache.shiro.authc.credential.PasswordService;
import org.apache.shiro.guice.aop.ShiroAopModule;
import org.apache.shiro.web.filter.mgt.FilterChainManager;
import org.apache.shiro.web.filter.mgt.FilterChainResolver;
import org.apache.shiro.web.mgt.WebSecurityManager;
import org.apache.shiro.web.servlet.ShiroFilter;

import com.gitplex.commons.loader.AbstractPluginModule;
import com.gitplex.commons.jetty.ServletConfigurator;

/**
 * NOTE: Do not forget to rename moduleClass property defined in the pom if you've renamed this class.
 *
 */
public class ShiroModule extends AbstractPluginModule {

	@Override
	protected void configure() {
		super.configure();
		
		// put your guice bindings here
		bind(WebSecurityManager.class).to(DefaultWebSecurityManager.class);
		bind(FilterChainResolver.class).to(DefaultFilterChainResolver.class);
		bind(BasicAuthenticationFilter.class);
		bind(PasswordService.class).to(DefaultPasswordService.class).in(Singleton.class);
		bind(ShiroFilter.class);
		 
		install(new ShiroAopModule());
		
		contribute(ServletConfigurator.class, ShiroServletConfigurator.class);
		
        contribute(FilterChainConfigurator.class, new FilterChainConfigurator() {

            @Override
            public void configure(FilterChainManager filterChainManager) {
            }
            
        });
		
	}

}
