package com.pmease.commons.hibernate;

import java.util.Collection;
import java.util.EnumSet;

import javax.servlet.DispatcherType;

import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlet.ServletContextHandler;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import com.pmease.commons.jetty.extensionpoints.ServletContextConfigurator;
import com.pmease.commons.loader.AbstractPlugin;

public class HibernatePlugin extends AbstractPlugin {

	private final PersistService persistService;
	
	private final HibernateFilter persistFilter;
	
	@Inject
	public HibernatePlugin(PersistService persistService, HibernateFilter persistFilter) {
		this.persistService = persistService;
		this.persistFilter = persistFilter;
	}
		
	@Override
	public void preStartDependents() {
		persistService.start();
	}

	@Override
	public void postStopDependents() {
		persistService.stop();
	}

	@Override
	public Collection<?> getExtensions() {
		return ImmutableList.of(new ServletContextConfigurator() {
			
			@Override
			public void configure(ServletContextHandler context) {
				FilterHolder filterHolder = new FilterHolder(persistFilter);
				context.addFilter(filterHolder, "/*", EnumSet.of(DispatcherType.REQUEST)); 
			}
		});
	}

}
