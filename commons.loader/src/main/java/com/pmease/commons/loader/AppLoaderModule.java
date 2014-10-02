package com.pmease.commons.loader;

import java.util.Collection;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.inject.Singleton;

import com.google.common.collect.ImmutableSet;
import com.google.common.eventbus.EventBus;
import com.google.inject.AbstractModule;
import com.google.inject.Provider;
import com.google.inject.multibindings.Multibinder;

public class AppLoaderModule extends AbstractModule {

	@Override
	protected void configure() {
		bindConstant().annotatedWith(AppName.class).to("Application");

		/*
		 * Bind at least one implementation provider in order to prevent Guice from complaining of 
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
	    
	    bind(EventBus.class).toInstance(new EventBus());
	    
	    bind(ExecutorService.class).toProvider(new Provider<ExecutorService>() {

			@Override
			public ExecutorService get() {
				return new ThreadPoolExecutor(0, Integer.MAX_VALUE,
			            300L, TimeUnit.SECONDS,
			            new SynchronousQueue<Runnable>());
			}
	    	
	    }).in(Singleton.class);
	}

	private static abstract interface DummyInterface {
		
	}
	
	private static class DummyClass implements DummyInterface {
		
	}
	
}
