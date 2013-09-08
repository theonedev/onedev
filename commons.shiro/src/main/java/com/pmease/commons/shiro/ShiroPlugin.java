package com.pmease.commons.shiro;

import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;

import javax.servlet.DispatcherType;

import org.apache.shiro.web.env.EnvironmentLoader;
import org.apache.shiro.web.env.EnvironmentLoaderListener;
import org.apache.shiro.web.servlet.ShiroFilter;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlet.ServletContextHandler;

import com.pmease.commons.jetty.extensionpoints.ServletContextConfigurator;
import com.pmease.commons.loader.AbstractPlugin;

public class ShiroPlugin extends AbstractPlugin {
	
	@Override
	public Collection<?> getExtensions() {
		return Arrays.asList(new ServletContextConfigurator() {
			
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
