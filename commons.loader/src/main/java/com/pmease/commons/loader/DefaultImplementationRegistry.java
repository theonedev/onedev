package com.pmease.commons.loader;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.pmease.commons.util.ClassUtils;

@Singleton
public class DefaultImplementationRegistry implements ImplementationRegistry {

	private Set<ImplementationProvider> providers;
	
	@Inject
	public DefaultImplementationRegistry(Set<ImplementationProvider> providers) {
		this.providers = providers;
	}
	
	@Override
	public Collection<Class<?>> getImplementations(Class<?> abstractClass) {
		Collection<Class<?>> implementations = new HashSet<Class<?>>();
		for (Class<?> subClass: ClassUtils.findImplementations(abstractClass, abstractClass)) {
			implementations.add(subClass);
		}
		for (ImplementationProvider provider: providers) {
			if (provider.getAbstractClass() == abstractClass)
				implementations.addAll(provider.getImplementations());
		}
		return implementations;
	}

}
