package com.gitplex.server.persistence;

import java.util.Collection;

public interface ModelProvider {
	Collection<Class<? extends AbstractEntity>> getModelClasses();
}
