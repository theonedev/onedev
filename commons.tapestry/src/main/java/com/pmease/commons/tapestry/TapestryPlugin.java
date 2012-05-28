package com.pmease.commons.tapestry;

import java.util.Collection;
import java.util.EnumSet;

import javax.inject.Inject;
import javax.servlet.DispatcherType;

import org.apache.tapestry5.TapestryFilter;
import org.apache.tapestry5.internal.InternalConstants;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlet.ServletContextHandler;

import com.google.common.collect.ImmutableList;
import com.pmease.commons.jetty.extensionpoints.ServletContextConfigurator;
import com.pmease.commons.loader.AbstractPlugin;

public class TapestryPlugin extends AbstractPlugin {
	
	private final TapestryFilter tapestryFilter;
	
	private final Package appPackage;

	@Inject
	public TapestryPlugin(TapestryFilter tapestryFilter, @Tapestry Package appPackage) {
		this.tapestryFilter = tapestryFilter;
		this.appPackage = appPackage;
	}
	
	@Override
	public Collection<?> getExtensions() {
		return ImmutableList.of(
				new ServletContextConfigurator() {

					@Override
					public void configure(ServletContextHandler context) {
						FilterHolder filterHolder = new FilterHolder(tapestryFilter);
						filterHolder.setName("app");
						context.setInitParameter(InternalConstants.TAPESTRY_APP_PACKAGE_PARAM, appPackage.getName());
						context.addFilter(filterHolder, "/*", EnumSet.of(DispatcherType.REQUEST)); 
					}
					
				}
			);
	}

}
