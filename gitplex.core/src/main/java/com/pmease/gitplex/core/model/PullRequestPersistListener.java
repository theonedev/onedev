package com.pmease.gitplex.core.model;

import java.io.Serializable;

import org.hibernate.CallbackException;
import org.hibernate.type.Type;

import com.pmease.commons.hibernate.HibernateListener;

public class PullRequestPersistListener implements HibernateListener {

	@Override
	public boolean onLoad(Object entity, Serializable id, Object[] state, String[] propertyNames, Type[] types)
			throws CallbackException {
		return false;
	}

	@Override
	public boolean onFlushDirty(Object entity, Serializable id, Object[] currentState, Object[] previousState,
			String[] propertyNames, Type[] types) throws CallbackException {
		return false;
	}

	@Override
	public boolean onSave(Object entity, Serializable id, Object[] state, String[] propertyNames, Type[] types)
			throws CallbackException {
		if (entity instanceof PullRequest) {
			for (int i=0; i<propertyNames.length; i++) {
				if (propertyNames[i].equals("idStr")) {
					state[i] = id.toString();
					return true;
				}
			}
		} 
		return false;
	}

	@Override
	public void onDelete(Object entity, Serializable id, Object[] state, String[] propertyNames, Type[] types)
			throws CallbackException {
	}

}
