package io.onedev.server.persistence;

import java.io.Serializable;
import java.util.Set;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import org.hibernate.Interceptor;
import org.hibernate.type.Type;

@Singleton
public class HibernateInterceptor implements Interceptor {

	private final Set<PersistListener> listeners;
	
	@Inject
	public HibernateInterceptor(Set<PersistListener> listeners) {
		this.listeners = listeners;
	}
	
	@Override
	public boolean onLoad(Object entity, Object id, Object[] state, String[] propertyNames,
			Type[] types) {
		boolean changed = false;
		for (PersistListener listener: listeners) {
			if (listener.onLoad(entity, (Serializable) id, state, propertyNames, types))
				changed = true;
		}
			
		return changed;
	}

	@Override
	public boolean onFlushDirty(Object entity, Object id, Object[] currentState,
			Object[] previousState, String[] propertyNames, Type[] types) {
		boolean changed = false;
		for (PersistListener listener: listeners) {
			if (listener.onFlushDirty(entity, (Serializable) id, currentState, previousState, propertyNames, types))
				changed = true;
		}
			
		return changed;
	}

	@Override
	public boolean onSave(Object entity, Object id, Object[] state, String[] propertyNames,
			Type[] types) {
		boolean changed = false;
		for (PersistListener listener: listeners) {
			if (listener.onSave(entity, (Serializable) id, state, propertyNames, types))
				changed = true;
		}
		
		return changed;
	}

	@Override
	public void onDelete(Object entity, Object id, Object[] state, String[] propertyNames,
			Type[] types) {
		for (PersistListener listener: listeners)
			listener.onDelete(entity, (Serializable) id, state, propertyNames, types);
	}

}
