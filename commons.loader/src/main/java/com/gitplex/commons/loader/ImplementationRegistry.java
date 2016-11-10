package com.gitplex.commons.loader;

import java.util.Collection;

public interface ImplementationRegistry {
	<T> Collection<Class<? extends T>> getImplementations(Class<T> abstractClass);
}
