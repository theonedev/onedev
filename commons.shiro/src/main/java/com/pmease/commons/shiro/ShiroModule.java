package com.pmease.commons.shiro;

import java.util.EnumSet;

import javax.inject.Singleton;
import javax.servlet.DispatcherType;

import org.apache.shiro.authc.credential.CredentialsMatcher;
import org.apache.shiro.authc.credential.DefaultPasswordService;
import org.apache.shiro.authc.credential.PasswordService;
import org.apache.shiro.guice.aop.ShiroAopModule;
import org.apache.shiro.web.env.EnvironmentLoader;
import org.apache.shiro.web.env.EnvironmentLoaderListener;
import org.apache.shiro.web.filter.mgt.FilterChainResolver;
import org.apache.shiro.web.mgt.WebSecurityManager;
import org.apache.shiro.web.servlet.ShiroFilter;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlet.ServletContextHandler;

import com.pmease.commons.jetty.ServletContextConfigurator;
import com.pmease.commons.loader.AbstractPluginModule;

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
		bind(CredentialsMatcher.class).to(DefaultPasswordMatcher.class);
		
		install(new ShiroAopModule());
		
		contribute(ServletContextConfigurator.class, new ServletContextConfigurator() {

			@Override
			public void configure(ServletContextHandler context) {
				context.setInitParameter(
						EnvironmentLoader.ENVIRONMENT_CLASS_PARAM, 
						DefaultWebEnvironment.class.getName());
				
				context.addEventListener(new EnvironmentLoaderListener());

				context.addFilter(new FilterHolder(new ShiroFilter()), "/*", EnumSet.allOf(DispatcherType.class));
			}
			
		});
	}

}
