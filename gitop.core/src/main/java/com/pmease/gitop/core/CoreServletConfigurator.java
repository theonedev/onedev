package com.pmease.gitop.core;

import java.util.EnumSet;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.DispatcherType;

import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlet.ServletContextHandler;

import com.pmease.commons.hibernate.HibernateFilter;
import com.pmease.commons.jetty.ServletConfigurator;

@Singleton
public class CoreServletConfigurator implements ServletConfigurator {

	private final HibernateFilter hibernateFilter;
	
	@Inject
	public CoreServletConfigurator(HibernateFilter hibernateFilter) {
		this.hibernateFilter = hibernateFilter;
	}
	
	@Override
	public void configure(ServletContextHandler context) {
		FilterHolder filterHolder = new FilterHolder(hibernateFilter);
		context.addFilter(filterHolder, "/*", EnumSet.of(DispatcherType.REQUEST));
	}

}
