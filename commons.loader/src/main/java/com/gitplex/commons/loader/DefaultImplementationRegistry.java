package com.gitplex.commons.loader;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.gitplex.commons.util.ClassUtils;

@Singleton
public class DefaultImplementationRegistry implements ImplementationRegistry {

	private Set<ImplementationProvider> providers;
	
	@Inject
	public DefaultImplementationRegistry(Set<ImplementationProvider> providers) {
		this.providers = providers;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public <T> Collection<Class<? extends T>> getImplementations(Class<T> abstractClass) {
		Collection<Class<? extends T>> implementations = new HashSet<>();
		for (Class<? extends T> subClass: ClassUtils.findImplementations(abstractClass, abstractClass)) {
			implementations.add(subClass);
		}
		for (ImplementationProvider provider: providers) {
			if (provider.getAbstractClass() == abstractClass) {
				for (Class<?> implementation: provider.getImplementations())
					implementations.add((Class<? extends T>) implementation);
			}
		}
		return implementations;
	}

}
