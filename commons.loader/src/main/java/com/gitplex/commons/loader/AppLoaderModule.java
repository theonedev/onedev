package com.gitplex.commons.loader;

import java.util.Collection;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;

import javax.inject.Singleton;

import com.google.common.collect.ImmutableSet;
import com.google.inject.AbstractModule;
import com.google.inject.Provider;
import com.google.inject.multibindings.Multibinder;

public class AppLoaderModule extends AbstractModule {

	@Override
	protected void configure() {
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
	    
	    bind(PluginManager.class).to(DefaultPluginManager.class);
	    bind(ImplementationRegistry.class).to(DefaultImplementationRegistry.class);
	    
	    bind(ForkJoinPool.class).toInstance(new ForkJoinPool());
	    
	    bind(ExecutorService.class).toProvider(new Provider<ExecutorService>() {

			@Override
			public ExecutorService get() {
				return Executors.newCachedThreadPool();
			}
	    	
	    }).in(Singleton.class);
	    
	    bind(ListenerRegistry.class).to(DefaultListenerRegistry.class);
	}

	private static abstract interface DummyInterface {
		
	}
	
	private static class DummyClass implements DummyInterface {
		
	}
	
}
