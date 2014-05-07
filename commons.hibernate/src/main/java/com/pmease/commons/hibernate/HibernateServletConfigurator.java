package com.pmease.commons.hibernate;

import java.util.EnumSet;

import javax.inject.Inject;
import javax.servlet.DispatcherType;

import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlet.ServletContextHandler;

import com.pmease.commons.jetty.ServletConfigurator;

public class HibernateServletConfigurator implements ServletConfigurator {

	private final HibernateFilter hibernateFilter;
	
	@Inject
	public HibernateServletConfigurator(HibernateFilter hibernateFilter) {
		this.hibernateFilter = hibernateFilter;
	}
	
	@Override
	public void configure(ServletContextHandler context) {
		FilterHolder filterHolder = new FilterHolder(hibernateFilter);
		context.addFilter(filterHolder, "/*", EnumSet.of(DispatcherType.REQUEST));
	}

}
