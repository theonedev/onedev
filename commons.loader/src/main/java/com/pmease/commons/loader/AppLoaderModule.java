package com.pmease.commons.loader;

import java.util.Collection;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableSet;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.SubscriberExceptionContext;
import com.google.common.eventbus.SubscriberExceptionHandler;
import com.google.inject.AbstractModule;
import com.google.inject.Provider;
import com.google.inject.multibindings.Multibinder;

public class AppLoaderModule extends AbstractModule {

	private static final Logger logger = LoggerFactory.getLogger(AppLoaderModule.class);
	
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
	    
	    bind(EventBus.class).toInstance(new EventBus(new SubscriberExceptionHandler() {
			
			@Override
			public void handleException(Throwable exception, SubscriberExceptionContext context) {
				logger.error("Could not dispatch event '" + context.getSubscriber() 
						+ "' to " + "'" + context.getSubscriberMethod() + "'", exception);
			}
			
		}));
	    bind(PluginManager.class).to(DefaultPluginManager.class);
	    bind(ImplementationRegistry.class).to(DefaultImplementationRegistry.class);
	    
	    bind(ExecutorService.class).toProvider(new Provider<ExecutorService>() {

			@Override
			public ExecutorService get() {
				return new ThreadPoolExecutor(0, Integer.MAX_VALUE, 300L, TimeUnit.SECONDS, new SynchronousQueue<Runnable>()) {

					@Override
					public void execute(final Runnable command) {
						final Object inheritableThreadLocalData = InheritableThreadLocalData.get();
						super.execute(new Runnable() {

							@Override
							public void run() {
								InheritableThreadLocalData.set(inheritableThreadLocalData);
								try {
									command.run();
								} finally {
									InheritableThreadLocalData.clear();
								}
							}
							
						});
					}
					
				};
			}
	    	
	    }).in(Singleton.class);
	}

	private static abstract interface DummyInterface {
		
	}
	
	private static class DummyClass implements DummyInterface {
		
	}
	
}
