package com.pmease.commons.hibernate;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.EnumSet;

import javax.servlet.DispatcherType;

import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlet.ServletContextHandler;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import com.google.inject.TypeLiteral;
import com.pmease.commons.jetty.extensionpoints.ServletContextConfigurator;
import com.pmease.commons.loader.AbstractPlugin;
import com.pmease.commons.util.ExceptionUtils;

public class HibernatePlugin extends AbstractPlugin {

	private final PersistService persistService;
	
	private final HibernateFilter hibernateFilter;

	private static Field typeLiteralTypeField;
	
	static {
		try {
			typeLiteralTypeField = TypeLiteral.class.getDeclaredField("type");
			typeLiteralTypeField.setAccessible(true);
			Field modifiersField = Field.class.getDeclaredField("modifiers");
			modifiersField.setAccessible(true);
			modifiersField.setInt(typeLiteralTypeField, Modifier.PROTECTED);
		} catch (Exception e) {
			throw ExceptionUtils.unchecked(e);
		}
		
	}

	@Inject
	public HibernatePlugin(PersistService persistService, HibernateFilter hibernateFilter) {
		this.persistService = persistService;
		this.hibernateFilter = hibernateFilter;
	}

	@Override
	public void start() {
		persistService.start();
	}

	@Override
	public void stop() {
		persistService.stop();
	}

	@Override
	public Collection<?> getExtensions() {
		return ImmutableList.of(new ServletContextConfigurator() {

			@Override
			public void configure(ServletContextHandler context) {
				FilterHolder filterHolder = new FilterHolder(hibernateFilter);
				context.addFilter(filterHolder, "/*", EnumSet.of(DispatcherType.REQUEST));
			}
		});
	}

}
