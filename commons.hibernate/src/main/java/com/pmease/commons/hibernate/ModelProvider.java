package com.pmease.commons.hibernate;

import java.util.Collection;

public interface ModelProvider {
	Collection<Class<? extends AbstractEntity>> getModelClasses();
}
