package io.onedev.server.persistence;

import java.io.Serializable;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.hibernate.CallbackException;
import org.hibernate.EmptyInterceptor;
import org.hibernate.type.Type;

@Singleton
public class HibernateInterceptor extends EmptyInterceptor {

	private static final long serialVersionUID = 1L;

	private final Set<PersistListener> listeners;
	
	@Inject
	public HibernateInterceptor(Set<PersistListener> listeners) {
		this.listeners = listeners;
	}
	
	@Override
	public boolean onLoad(Object entity, Serializable id, Object[] state, String[] propertyNames,
			Type[] types) throws CallbackException {
		boolean changed = false;
		for (PersistListener listener: listeners) {
			if (listener.onLoad(entity, id, state, propertyNames, types))
				changed = true;
		}
			
		return changed;
	}

	@Override
	public boolean onFlushDirty(Object entity, Serializable id, Object[] currentState,
			Object[] previousState, String[] propertyNames, Type[] types) throws CallbackException {
		boolean changed = false;
		for (PersistListener listener: listeners) {
			if (listener.onFlushDirty(entity, id, currentState, previousState, propertyNames, types))
				changed = true;
		}
			
		return changed;
	}

	@Override
	public boolean onSave(Object entity, Serializable id, Object[] state, String[] propertyNames,
			Type[] types) throws CallbackException {
		boolean changed = false;
		for (PersistListener listener: listeners) {
			if (listener.onSave(entity, id, state, propertyNames, types))
				changed = true;
		}
		
		return changed;
	}

	@Override
	public void onDelete(Object entity, Serializable id, Object[] state, String[] propertyNames,
			Type[] types) throws CallbackException {
		for (PersistListener listener: listeners)
			listener.onDelete(entity, id, state, propertyNames, types);
	}

}
