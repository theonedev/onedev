package com.pmease.commons.shiro;

import java.util.EnumSet;

import javax.servlet.DispatcherType;

import org.apache.shiro.web.env.EnvironmentLoader;
import org.apache.shiro.web.env.EnvironmentLoaderListener;
import org.apache.shiro.web.servlet.ShiroFilter;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlet.ServletContextHandler;

import com.pmease.commons.jetty.extensionpoints.ServletContextConfigurator;

public class ShiroServletContextConfigurator implements ServletContextConfigurator {

	@Override
	public void configure(ServletContextHandler context) {
		context.setInitParameter(
				EnvironmentLoader.ENVIRONMENT_CLASS_PARAM, 
				DefaultWebEnvironment.class.getName());
		
		context.addEventListener(new EnvironmentLoaderListener());

		context.addFilter(new FilterHolder(new ShiroFilter()), "/*", EnumSet.allOf(DispatcherType.class));
	}

}
