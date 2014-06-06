package com.pmease.commons.loader;

import java.util.Collection;

public interface ImplementationProvider {
	
	Class<?> getAbstractClass();
	
	Collection<Class<?>> getImplementations();
}
