package com.pmease.commons.loader;

import java.util.Collection;

import com.google.inject.ImplementedBy;

@ImplementedBy(DefaultImplementationRegistry.class)
public interface ImplementationRegistry {
	<T> Collection<Class<? extends T>> getImplementations(Class<T> abstractClass);
}
