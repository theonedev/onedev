package io.onedev.server.persistence;

import java.io.Serializable;

import org.hibernate.type.Type;

public interface PersistListener {

	boolean onLoad(Object entity, Serializable id, Object[] state, String[] propertyNames,
			Type[] types);

	boolean onFlushDirty(Object entity, Serializable id, Object[] currentState,
			Object[] previousState, String[] propertyNames, Type[] types);

	boolean onSave(Object entity, Serializable id, Object[] state, String[] propertyNames,
			Type[] types);

	void onDelete(Object entity, Serializable id, Object[] state, String[] propertyNames,
			Type[] types);
	
}
