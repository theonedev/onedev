package com.pmease.commons.tapestry.persistence;

import java.io.Serializable;

import javax.inject.Provider;

import org.apache.tapestry5.ValueEncoder;
import org.apache.tapestry5.ioc.internal.util.InternalUtils;
import org.apache.tapestry5.ioc.services.PropertyAccess;
import org.apache.tapestry5.ioc.services.PropertyAdapter;
import org.apache.tapestry5.ioc.services.TypeCoercer;
import org.hibernate.Session;
import org.hibernate.mapping.PersistentClass;
import org.hibernate.mapping.Property;
import org.slf4j.Logger;

import com.pmease.commons.hibernate.SessionProvider;

public final class EntityValueEncoder<E> implements ValueEncoder<E> {
	private final Class<E> entityClass;

	private final Provider<Session> sessionProvider;

	private final TypeCoercer typeCoercer;

	private final PropertyAdapter propertyAdapter;

	private final Logger logger;

	public EntityValueEncoder(Class<E> entityClass,
			PersistentClass persistentClass, SessionProvider sessionProvider,
			PropertyAccess propertyAccess, TypeCoercer typeCoercer,
			Logger logger) {
		this.sessionProvider = sessionProvider;
		this.entityClass = entityClass;
		this.typeCoercer = typeCoercer;
		this.logger = logger;

		Property property = persistentClass.getIdentifierProperty();

		propertyAdapter = propertyAccess.getAdapter(this.entityClass)
				.getPropertyAdapter(property.getName());
	}

	public String toClient(E value) {
		if (value == null)
			return null;

		Object id = propertyAdapter.get(value);

		if (id == null) {
			return null;
		}

		return typeCoercer.coerce(id, String.class);
	}

	@SuppressWarnings("unchecked")
	public E toValue(String clientValue) {
		if (InternalUtils.isBlank(clientValue))
			return null;

		Object id = null;

		try {

			id = typeCoercer.coerce(clientValue, propertyAdapter.getType());
		} catch (Exception ex) {
			throw new RuntimeException(
					String.format(
							"Exception converting '%s' to instance of %s (id type for entity %s): %s",
							clientValue, propertyAdapter.getType().getName(),
							entityClass.getName(), InternalUtils.toMessage(ex)),
					ex);
		}

		Serializable ser = (Serializable) id;

		E result = (E) sessionProvider.get().get(entityClass, ser);

		if (result == null) {
			// We don't identify the entity type in the message because the
			// logger is based on the
			// entity type.
			logger.error(String
					.format("Unable to convert client value '%s' into an entity instance.",
							clientValue));
		}

		return result;
	}

}
