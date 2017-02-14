package com.gitplex.commons.shiro;

import java.util.EnumSet;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.DispatcherType;

import org.apache.shiro.web.env.EnvironmentLoader;
import org.apache.shiro.web.env.EnvironmentLoaderListener;
import org.apache.shiro.web.servlet.ShiroFilter;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlet.ServletContextHandler;

import com.gitplex.commons.jetty.ServletConfigurator;

@Singleton
public class ShiroServletConfigurator implements ServletConfigurator {

	private final ShiroFilter shiroFilter;
	
	@Inject
	public ShiroServletConfigurator(ShiroFilter shiroFilter) {
		this.shiroFilter = shiroFilter;
	}
	
	@Override
	public void configure(ServletContextHandler context) {
		context.setInitParameter(
				EnvironmentLoader.ENVIRONMENT_CLASS_PARAM, 
				DefaultWebEnvironment.class.getName());
		
		context.addEventListener(new EnvironmentLoaderListener());

		FilterHolder filterHolder = new FilterHolder(shiroFilter);
		context.addFilter(filterHolder, "/*", EnumSet.allOf(DispatcherType.class));
	}

}
