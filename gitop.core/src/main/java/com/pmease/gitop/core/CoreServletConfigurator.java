package com.pmease.gitop.core;

import java.util.EnumSet;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.DispatcherType;

import org.apache.shiro.web.env.EnvironmentLoader;
import org.apache.shiro.web.env.EnvironmentLoaderListener;
import org.apache.shiro.web.servlet.ShiroFilter;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlet.ServletContextHandler;

import com.pmease.commons.hibernate.HibernateFilter;
import com.pmease.commons.jetty.ServletConfigurator;
import com.pmease.commons.shiro.DefaultWebEnvironment;

@Singleton
public class CoreServletConfigurator implements ServletConfigurator {

	private final ShiroFilter shiroFilter;
	
	private final GitFilter gitFilter;
	
	private final HibernateFilter hibernateFilter;
	
	@Inject
	public CoreServletConfigurator(HibernateFilter hibernateFilter, ShiroFilter shiroFilter, GitFilter gitFilter) {
		this.hibernateFilter = hibernateFilter;
		this.shiroFilter = shiroFilter;
		this.gitFilter = gitFilter;
	}
	
	@Override
	public void configure(ServletContextHandler context) {
		FilterHolder filterHolder = new FilterHolder(hibernateFilter);
		context.addFilter(filterHolder, "/*", EnumSet.of(DispatcherType.REQUEST));

		context.setInitParameter(
				EnvironmentLoader.ENVIRONMENT_CLASS_PARAM, 
				DefaultWebEnvironment.class.getName());
		
		context.addEventListener(new EnvironmentLoaderListener());

		filterHolder = new FilterHolder(shiroFilter);
		context.addFilter(filterHolder, "/*", EnumSet.allOf(DispatcherType.class));
		
		filterHolder = new FilterHolder(gitFilter);
		context.addFilter(filterHolder, "/*", EnumSet.allOf(DispatcherType.class));
		
		/*
		ServletHolder servletHolder = new ServletHolder(new GitServlet());
		servletHolder.setInitParameter("export-all", "1");
		servletHolder.setInitParameter("base-path", "w:\\temp\\storage\\1");
		context.addServlet(servletHolder, "/git/*");
		*/
	}

}
