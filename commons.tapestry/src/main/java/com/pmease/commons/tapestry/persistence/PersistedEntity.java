package com.pmease.commons.tapestry.persistence;

import org.apache.tapestry5.annotations.ImmutableSessionPersistedObject;
import org.hibernate.Session;

import java.io.Serializable;

/**
 * Encapsulates a Hibernate entity name with an entity id.
 */
@ImmutableSessionPersistedObject
public class PersistedEntity implements Serializable {

	private static final long serialVersionUID = 1L;

	private final String entityName;

	private final Serializable id;

	public PersistedEntity(String entityName, Serializable id) {
		this.entityName = entityName;
		this.id = id;
	}

	public Object restore(Session session) {
		System.out.println("loading " + entityName + " " + id);
		return session.load(entityName, id);
	}

	@Override
	public String toString() {
		return String.format("<PersistedEntity: %s(%s)>", entityName, id);
	}
}
