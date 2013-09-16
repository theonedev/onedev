package com.pmease.commons.loader;

import java.util.Collection;

import com.google.common.collect.ImmutableSet;
import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;

public class AppLoaderModule extends AbstractModule {

	@Override
	protected void configure() {
		bindConstant().annotatedWith(AppName.class).to("Application");

		/*
		 * Bind at least implementation provider in order to prevent Guice from complaining of 
		 * not bound when injecting Set<ImplementationProvider>
		 */
		Multibinder<ImplementationProvider> binder = Multibinder.newSetBinder(binder(), ImplementationProvider.class);
	    binder.addBinding().toInstance(new ImplementationProvider() {
			
			@Override
			public Collection<Class<?>> getImplementations() {
				return ImmutableSet.<Class<?>>of(DummyClass.class);
			}
			
			@Override
			public Class<?> getAbstractClass() {
				return DummyInterface.class;
			}
			
		});
	}

	private static abstract interface DummyInterface {
		
	}
	
	private static class DummyClass implements DummyInterface {
		
	}
	
}
