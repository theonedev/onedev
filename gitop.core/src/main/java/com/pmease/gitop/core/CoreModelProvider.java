package com.pmease.gitop.core;

import java.util.Collection;
import java.util.HashSet;

import com.pmease.commons.hibernate.AbstractEntity;
import com.pmease.commons.hibernate.ModelProvider;
import com.pmease.commons.util.ClassUtils;
import com.pmease.gitop.core.model.ModelLocator;

public class CoreModelProvider implements ModelProvider {

	@Override
	public Collection<Class<? extends AbstractEntity>> getModelClasses() {
		Collection<Class<? extends AbstractEntity>> modelClasses = 
				new HashSet<Class<? extends AbstractEntity>>();
		modelClasses.addAll(ClassUtils.findSubClasses(AbstractEntity.class, ModelLocator.class));
		return modelClasses;
	}

}
