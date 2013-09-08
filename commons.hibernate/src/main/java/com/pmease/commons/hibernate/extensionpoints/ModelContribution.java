package com.pmease.commons.hibernate.extensionpoints;

import java.util.Collection;

import com.pmease.commons.hibernate.AbstractEntity;

public interface ModelContribution {
	Collection<Class<? extends AbstractEntity>> getModelClasses();
}
