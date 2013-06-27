package com.pmease.commons.persistence.extensionpoints;

import java.util.Collection;

import com.pmease.commons.persistence.AbstractEntity;

public interface ModelContribution {
	Collection<Class<? extends AbstractEntity>> getModelClasses();
}
