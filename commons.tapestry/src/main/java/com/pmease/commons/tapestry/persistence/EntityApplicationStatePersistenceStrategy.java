package com.pmease.commons.tapestry.persistence;

import java.io.Serializable;

import org.apache.tapestry5.internal.services.SessionApplicationStatePersistenceStrategy;
import org.apache.tapestry5.services.ApplicationStateCreator;
import org.apache.tapestry5.services.Request;
import org.hibernate.HibernateException;

import com.pmease.commons.hibernate.SessionProvider;

public class EntityApplicationStatePersistenceStrategy extends SessionApplicationStatePersistenceStrategy {

	private final SessionProvider sessionProvider;

	public EntityApplicationStatePersistenceStrategy(Request request,
			SessionProvider sessionProvider) {
		super(request);
		this.sessionProvider = sessionProvider;
	}

	@SuppressWarnings("unchecked")
	public <T> T get(Class<T> ssoClass, ApplicationStateCreator<T> creator) {
		final Object persistedValue = getOrCreate(ssoClass, creator);

		if (persistedValue instanceof PersistedEntity) {
			final PersistedEntity persisted = (PersistedEntity) persistedValue;

			Object restored = persisted.restore(sessionProvider.get());

			// shall we maybe throw an exception instead?
			if (restored == null) {
				set(ssoClass, null);
				return (T) getOrCreate(ssoClass, creator);
			}

			return (T) restored;
		}

		return (T) persistedValue;
	}

	public <T> void set(Class<T> ssoClass, T sso) {
		final String key = buildKey(ssoClass);
		Object entity;

		if (sso != null) {
			try {
				final String entityName = sessionProvider.get().getEntityName(sso);
				final Serializable id = sessionProvider.get().getIdentifier(sso);

				entity = new PersistedEntity(entityName, id);
			} catch (final HibernateException ex) {
				entity = sso;
			}
		} else {
			entity = sso;
		}

		getSession().setAttribute(key, entity);
	}

}