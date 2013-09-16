package com.pmease.commons.loader;

import java.util.Collection;

import com.google.inject.ImplementedBy;

@ImplementedBy(DefaultImplementationRegistry.class)
public interface ImplementationRegistry {
	Collection<Class<?>> getImplementations(Class<?> abstractClass);
}
