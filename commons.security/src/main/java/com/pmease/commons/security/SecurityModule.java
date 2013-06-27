package com.pmease.commons.security;

import javax.inject.Singleton;

import org.apache.shiro.authc.credential.CredentialsMatcher;
import org.apache.shiro.authc.credential.DefaultPasswordService;
import org.apache.shiro.authc.credential.PasswordService;
import org.apache.shiro.mgt.RememberMeManager;
import org.apache.shiro.web.filter.mgt.FilterChainResolver;
import org.apache.shiro.web.mgt.WebSecurityManager;

import com.pmease.commons.loader.AbstractPlugin;
import com.pmease.commons.loader.AbstractPluginModule;

/**
 * NOTE: Do not forget to rename moduleClass property defined in the pom if you've renamed this class.
 *
 */
public class SecurityModule extends AbstractPluginModule {

	@Override
	protected void configure() {
		super.configure();
		
		// put your guice bindings here
		bind(WebSecurityManager.class).to(DefaultWebSecurityManager.class);
		bind(FilterChainResolver.class).to(DefaultFilterChainResolver.class);
		bind(BasicAuthenticationFilter.class);
		bind(RememberMeManager.class).to(DefaultRememberMeManager.class);
		bind(PasswordService.class).to(DefaultPasswordService.class).in(Singleton.class);
		bind(CredentialsMatcher.class).to(DefaultPasswordMatcher.class);
	}

	@Override
	protected Class<? extends AbstractPlugin> getPluginClass() {
		return SecurityPlugin.class;
	}

}
