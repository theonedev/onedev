package com.pmease.commons.jersey;

import io.dropwizard.jersey.DropwizardResourceConfig;
import io.dropwizard.jersey.jackson.JacksonMessageBodyProvider;

import java.util.Set;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import javax.validation.Validator;

import com.codahale.metrics.MetricRegistry;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Injector;
import com.pmease.commons.util.ClassUtils;
import com.sun.jersey.api.core.DefaultResourceConfig;
import com.sun.jersey.api.core.ResourceConfig;

@Singleton
public class JerseyEnvironment implements Provider<ResourceConfig> {
	
	private final Injector injector;
	
	private final DefaultResourceConfig resourceConfig;
	
	@Inject
	public JerseyEnvironment(Injector injector, MetricRegistry metricRegistry, Validator validator, 
			ObjectMapper objectMapper, Set<JerseyConfigurator> configurators) {
		this.injector = injector;
		
		resourceConfig = new DropwizardResourceConfig(metricRegistry);
		
		resourceConfig.getSingletons().add(new JacksonMessageBodyProvider(objectMapper, validator));
		for (JerseyConfigurator configurator: configurators)
			configurator.configure(this);
	}

	public void addComponent(Class<?> componentClass) {
		resourceConfig.getSingletons().add(injector.getInstance(componentClass));
	}

	public void addComponentFromPackage(Class<?> packageLocator) {
		for (Class<?> each: ClassUtils.findImplementations(Object.class, packageLocator)) {
			if (DefaultResourceConfig.isProviderClass(each) || DefaultResourceConfig.isRootResourceClass(each))
				addComponent(each);
		}
	}

	@Override
	public ResourceConfig get() {
		return resourceConfig;
	}
	
}
