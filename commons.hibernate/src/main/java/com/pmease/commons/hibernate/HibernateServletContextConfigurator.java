package com.pmease.commons.hibernate;

import java.util.EnumSet;

import javax.inject.Inject;
import javax.servlet.DispatcherType;

import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlet.ServletContextHandler;

import com.pmease.commons.jetty.ServletContextConfigurator;

public class HibernateServletContextConfigurator implements ServletContextConfigurator {

	private final HibernateFilter hibernateFilter;
	
	@Inject
	public HibernateServletContextConfigurator(HibernateFilter hibernateFilter) {
		this.hibernateFilter = hibernateFilter;
	}
	
	@Override
	public void configure(ServletContextHandler context) {
		FilterHolder filterHolder = new FilterHolder(hibernateFilter);
		context.addFilter(filterHolder, "/*", EnumSet.of(DispatcherType.REQUEST));
	}

}
