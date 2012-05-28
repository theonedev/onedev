package com.pmease.commons.tapestry.persistence;

import java.io.Serializable;

import org.apache.tapestry5.internal.services.AbstractSessionPersistentFieldStrategy;
import org.apache.tapestry5.services.Request;

import com.pmease.commons.hibernate.SessionProvider;

public class EntityPersistentFieldStrategy extends AbstractSessionPersistentFieldStrategy {
	
	private final SessionProvider sessionProvider;

	public EntityPersistentFieldStrategy(SessionProvider sessionProvider, Request request) {
		super("entity:", request);

		this.sessionProvider = sessionProvider;
	}

	@Override
	protected Object convertApplicationValueToPersisted(Object newValue) {
		String entityName = sessionProvider.get().getEntityName(newValue);
		Serializable id = sessionProvider.get().getIdentifier(newValue);

		return new PersistedEntity(entityName, id);
	}

	@Override
	protected Object convertPersistedToApplicationValue(Object persistedValue) {
		PersistedEntity persisted = (PersistedEntity) persistedValue;

		return persisted.restore(sessionProvider.get());
	}
	
}
