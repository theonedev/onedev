package io.onedev.server.entityreference;

import javax.annotation.Nullable;

import io.onedev.server.model.AbstractEntity;

public interface ReferencedFromAware<T extends AbstractEntity> {

	@Nullable
	T getReferencedFrom();
	
}
